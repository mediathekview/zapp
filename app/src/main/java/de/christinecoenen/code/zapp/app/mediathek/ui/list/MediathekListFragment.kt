package de.christinecoenen.code.zapp.app.mediathek.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.api.request.MediathekChannel
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.MediathekDetailActivity.Companion.getStartIntent
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.FooterLoadStateAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.ListItemListener
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekItemAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowComparator
import de.christinecoenen.code.zapp.databinding.FragmentMediathekListBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.net.UnknownServiceException
import javax.net.ssl.SSLHandshakeException


class MediathekListFragment : Fragment(), ListItemListener, OnRefreshListener {

	companion object {

		val instance
			get() = MediathekListFragment()

	}

	private var _binding: FragmentMediathekListBinding? = null
	private val binding: FragmentMediathekListBinding
		get() = _binding!!

	private val bottomSheetBehavior by lazy { BottomSheetBehavior.from(binding.filterBottomSheet) }

	private val viewmodel: MediathekListFragmentViewModel by viewModel()
	private lateinit var backPressedCallback: OnBackPressedCallback
	private lateinit var adapter: MediathekItemAdapter

	private var longClickShow: MediathekShow? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setHasOptionsMenu(true)

		// close bottom sheet first, before using system back button setting
		backPressedCallback = object : OnBackPressedCallback(false) {
			override fun handleOnBackPressed() {
				bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
			}
		}

		requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMediathekListBinding.inflate(inflater, container, false)

		val layoutManager = LinearLayoutManager(binding.root.context)
		binding.list.layoutManager = layoutManager

		binding.filter.search.addTextChangedListener { editable ->
			viewmodel.setSearchQueryFilter(editable.toString())
		}
		binding.refreshLayout.setOnRefreshListener(this)
		binding.refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary)

		createChannelFilterView(inflater)

		// only consume backPressedCallback when bottom sheet is not collapsed
		bottomSheetBehavior.addBottomSheetCallback(object :
			BottomSheetBehavior.BottomSheetCallback() {
			override fun onStateChanged(bottomSheet: View, newState: Int) {
				backPressedCallback.isEnabled = newState != BottomSheetBehavior.STATE_COLLAPSED
			}

			override fun onSlide(bottomSheet: View, slideOffset: Float) {}
		})

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewmodel.isFilterApplied.observe(viewLifecycleOwner) { onIsFilterAppliedChanged() }

		adapter = MediathekItemAdapter(MediathekShowComparator, this@MediathekListFragment)

		binding.list.adapter = adapter.withLoadStateFooter(FooterLoadStateAdapter(adapter::retry))

		viewLifecycleOwner.lifecycleScope.launch {
			viewmodel.flow.collectLatest { pagingData ->
				adapter.submitData(pagingData)
			}
		}

		viewLifecycleOwner.lifecycleScope.launch {
			adapter.loadStateFlow
				.drop(1)
				.map { it.refresh }
				.distinctUntilChanged()
				.collectLatest { refreshState ->
					binding.refreshLayout.isRefreshing = refreshState is LoadState.Loading
					binding.error.isVisible = refreshState is LoadState.Error
					updateNoShowsMessage(refreshState)

					when (refreshState) {
						is LoadState.Error -> onMediathekLoadErrorChanged(refreshState.error)
						is LoadState.NotLoading -> binding.list.scrollToPosition(0)
						is LoadState.Loading -> Unit
					}
				}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater.inflate(R.menu.fragment_mediathek_list, menu)
	}

	override fun onPrepareOptionsMenu(menu: Menu) {
		super.onPrepareOptionsMenu(menu)

		val filterIconResId = if (viewmodel.isFilterApplied.value == true) {
			R.drawable.ic_sharp_filter_list_off_24
		} else {
			R.drawable.ic_sharp_filter_list_24
		}
		val filterItem = menu.findItem(R.id.menu_filter)
		filterItem.setIcon(filterIconResId)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_filter -> {
				onFilterMenuClicked()
				true
			}
			R.id.menu_refresh -> {
				onRefresh()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun onShowClicked(show: MediathekShow) {
		startActivity(getStartIntent(context, show))
	}

	override fun onShowLongClicked(show: MediathekShow, view: View) {
		longClickShow = show

		PopupMenu(context, view, Gravity.TOP or Gravity.END).apply {
			inflate(R.menu.activity_mediathek_detail)
			show()
			setOnMenuItemClickListener(::onContextMenuItemClicked)
		}
	}

	override fun onRefresh() {
		adapter.refresh()
	}

	private fun onFilterMenuClicked() {
		if (viewmodel.isFilterApplied.value == true) {
			viewmodel.clearFilter()
		} else {
			toggleFilterBottomSheet()
		}
	}

	private fun onContextMenuItemClicked(menuItem: MenuItem): Boolean {
		when (menuItem.itemId) {
			R.id.menu_share -> {
				startActivity(
					Intent.createChooser(
						longClickShow!!.shareIntentPlain,
						getString(R.string.action_share)
					)
				)
				return true
			}
		}
		return false
	}

	private fun onIsFilterAppliedChanged() {
		requireActivity().invalidateOptionsMenu()
	}

	private fun onMediathekLoadErrorChanged(e: Throwable) {
		if (e is SSLHandshakeException || e is UnknownServiceException) {
			showError(R.string.error_mediathek_ssl_error)
		} else {
			showError(R.string.error_mediathek_info_not_available)
		}
	}

	private fun toggleFilterBottomSheet() {
		if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
			bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
		} else {
			bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
		}
	}

	private fun showError(messageResId: Int) {
		binding.error.setText(messageResId)
		binding.error.visibility = View.VISIBLE
	}

	private fun updateNoShowsMessage(loadState: LoadState) {
		Timber.d(
			"updateNoShowsMessage: adapter.itemCount %d - loadState %s",
			adapter.itemCount,
			loadState
		)
		val isAdapterEmpty = adapter.itemCount == 0 && loadState is LoadState.NotLoading
		binding.noShows.isVisible = isAdapterEmpty
	}

	private fun createChannelFilterView(inflater: LayoutInflater) {
		for (channel in MediathekChannel.values()) {
			// create view
			val chip = inflater.inflate(
				R.layout.view_mediathek_filter_channel_chip,
				binding.filter.channels,
				false
			) as Chip

			// view properties
			chip.text = channel.apiId

			// ui listeners
			chip.setOnCheckedChangeListener { _, isChecked ->
				onChannelFilterCheckChanged(channel, isChecked)
			}
			chip.setOnLongClickListener {
				onChannelFilterLongClick(channel)
				true
			}

			// viewmodel listener
			viewmodel.channelFilter.getValue(channel).observe(viewLifecycleOwner) { isChecked ->
				chip.isChecked = isChecked
			}

			// add to hierarchy
			binding.filter.channels.addView(chip)
		}
	}

	private fun onChannelFilterCheckChanged(channel: MediathekChannel, isChecked: Boolean) {
		viewmodel.setChannelFilter(channel, isChecked)
	}

	private fun onChannelFilterLongClick(clickedChannel: MediathekChannel) {
		for (channel in MediathekChannel.values()) {
			val isChecked = clickedChannel == channel
			viewmodel.setChannelFilter(channel, isChecked)
		}
	}
}
