package de.christinecoenen.code.zapp.app.mediathek.ui.list

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.api.request.MediathekChannel
import de.christinecoenen.code.zapp.app.mediathek.api.result.QueryInfoResult
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.FooterLoadStateAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.ListItemListener
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekItemAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowComparator
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.UnknownServiceException
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*
import javax.net.ssl.SSLHandshakeException


class MediathekListFragment : Fragment(), ListItemListener, OnRefreshListener {

	private var _binding: MediathekListFragmentBinding? = null
	private val binding: MediathekListFragmentBinding
		get() = _binding!!

	private val numberFormat = NumberFormat.getInstance(Locale.getDefault())
	private val queryInfoDateFormatter = DateFormat.getDateTimeInstance(
		DateFormat.SHORT,
		DateFormat.SHORT
	)

	private val bottomSheetBehavior by lazy { BottomSheetBehavior.from(binding.filterBottomSheet) }

	private val viewmodel: MediathekListFragmentViewModel by viewModel()
	private lateinit var adapter: MediathekItemAdapter

	private var longClickShow: MediathekShow? = null

	private val backPressedCallback = object : OnBackPressedCallback(false) {
		override fun handleOnBackPressed() {
			// close bottom sheet first, before using system back button setting
			bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setHasOptionsMenu(true)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = MediathekListFragmentBinding.inflate(inflater, container, false)

		val layoutManager = LinearLayoutManager(binding.root.context)
		binding.list.layoutManager = layoutManager

		binding.filter.search.addTextChangedListener { editable ->
			viewmodel.setSearchQueryFilter(editable.toString())
		}
		binding.refreshLayout.setOnRefreshListener(this)
		binding.refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary)

		setUpLengthFilter()
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
		viewmodel.queryInfoResult.observe(viewLifecycleOwner, ::onQueryInfoResultChanged)


		adapter = MediathekItemAdapter(
			lifecycleScope,
			MediathekShowComparator,
			this@MediathekListFragment
		)

		binding.list.adapter = adapter.withLoadStateFooter(FooterLoadStateAdapter(adapter::retry))

		viewLifecycleOwner.lifecycleScope.launch {
			viewmodel.pageFlow.collectLatest { pagingData ->
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

	override fun onResume() {
		super.onResume()
		requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
	}

	override fun onPause() {
		super.onPause()
		backPressedCallback.remove()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater.inflate(R.menu.mediathek_list_fragment, menu)
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
		val directions = MediathekListFragmentDirections.toMediathekDetailFragment(show)
		findNavController().navigate(directions)
	}

	override fun onShowLongClicked(show: MediathekShow, view: View) {
		longClickShow = show

		PopupMenu(context, view, Gravity.TOP or Gravity.END).apply {
			inflate(R.menu.mediathek_detail_fragment)
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
				longClickShow?.shareExternally(requireContext())
				return true
			}
		}
		return false
	}

	private fun onQueryInfoResultChanged(queryInfoResult: QueryInfoResult?) {
		if (queryInfoResult == null) {
			binding.filter.queryInfo.visibility = View.INVISIBLE
			return
		}

		val date = Date(queryInfoResult.filmlisteTimestamp * 1000)
		val queryInfoMessage = getString(
			R.string.fragment_mediathek_query_info,
			numberFormat.format(queryInfoResult.totalResults),
			queryInfoDateFormatter.format(date)
		)

		binding.filter.queryInfo.text = queryInfoMessage
		binding.filter.queryInfo.visibility = View.VISIBLE
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
		val isAdapterEmpty = adapter.itemCount == 0 && loadState is LoadState.NotLoading
		binding.noShows.isVisible = isAdapterEmpty
	}

	private fun setUpLengthFilter() {
		val showLengthLabelFormatter =
			ShowLengthLabelFormatter(binding.filter.showLengthSlider.valueTo)

		updateLengthFilterLabels(showLengthLabelFormatter)
		binding.filter.showLengthSlider.setLabelFormatter(showLengthLabelFormatter)

		// from ui to viewmodel
		binding.filter.showLengthSlider.addOnChangeListener { rangeSlider, _, fromUser ->

			updateLengthFilterLabels(showLengthLabelFormatter)

			if (fromUser) {
				val min = rangeSlider.values[0] * 60
				val max =
					if (rangeSlider.values[1] == rangeSlider.valueTo) null else rangeSlider.values[1] * 60
				viewmodel.setLengthFilter(min, max)
			}
		}

		// from viewmodel to ui
		viewmodel.lengthFilter.observe(viewLifecycleOwner) { lengthFilter ->
			val min = lengthFilter.minDurationMinutes
			val max = lengthFilter.maxDurationMinutes ?: binding.filter.showLengthSlider.valueTo
			binding.filter.showLengthSlider.setValues(min, max)
		}
	}

	private fun createChannelFilterView(inflater: LayoutInflater) {
		val chipMap = mutableMapOf<MediathekChannel, Chip>()

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

			// add to hierarchy
			binding.filter.channels.addView(chip)

			// cache for listeners
			chipMap[channel] = chip
		}

		// viewmodel listener
		viewmodel.channelFilter.observe(viewLifecycleOwner) { channelFilter ->
			for (filterItem in channelFilter) {
				val chip = chipMap[filterItem.key]!!
				if (chip.isChecked != filterItem.value) {
					chip.isChecked = filterItem.value
				}
			}
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

	private fun updateLengthFilterLabels(formatter: ShowLengthLabelFormatter) {
		binding.filter.showLengthLabelMin.text =
			formatter.getFormattedValue(binding.filter.showLengthSlider.values[0])
		binding.filter.showLengthLabelMax.text =
			formatter.getFormattedValue(binding.filter.showLengthSlider.values[1])
	}
}
