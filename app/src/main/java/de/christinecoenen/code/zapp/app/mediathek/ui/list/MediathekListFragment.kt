package de.christinecoenen.code.zapp.app.mediathek.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.api.result.QueryInfoResult
import de.christinecoenen.code.zapp.app.mediathek.ui.helper.ShowMenuHelper
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.FooterLoadStateAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowListItemListener
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.PagedMediathekShowListAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.filter.MediathekFilterViewModel
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentBinding
import de.christinecoenen.code.zapp.databinding.ViewNoShowsBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.net.UnknownServiceException
import javax.net.ssl.SSLHandshakeException


class MediathekListFragment : Fragment(),
	MenuProvider,
	MediathekShowListItemListener,
	OnRefreshListener {

	private var _binding: MediathekListFragmentBinding? = null
	private val binding: MediathekListFragmentBinding get() = _binding!!

	private var _noShowsBinding: ViewNoShowsBinding? = null
	private val noShowsBinding: ViewNoShowsBinding get() = _noShowsBinding!!

	private var _bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>? = null
	private val bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
		get() = _bottomSheetBehavior!!

	private val filterViewModel: MediathekFilterViewModel by activityViewModel()
	private val viewmodel: MediathekListFragmentViewModel by viewModel {
		parametersOf(
			filterViewModel.searchQuery,
			filterViewModel.lengthFilter,
			filterViewModel.channelFilter
		)
	}
	private lateinit var adapter: PagedMediathekShowListAdapter

	private val backPressedCallback = object : OnBackPressedCallback(false) {
		override fun handleOnBackPressed() {
			// close bottom sheet first, before using system back button setting
			bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = MediathekListFragmentBinding.inflate(inflater, container, false)
		_noShowsBinding = ViewNoShowsBinding.bind(binding.root)

		val layoutManager = LinearLayoutManager(binding.root.context)
		binding.list.layoutManager = layoutManager

		binding.refreshLayout.setOnRefreshListener(this)
		binding.refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary)

		// only consume backPressedCallback when bottom sheet is not collapsed
		_bottomSheetBehavior = BottomSheetBehavior.from(binding.filterBottomSheet)
		bottomSheetBehavior.addBottomSheetCallback(object :
			BottomSheetBehavior.BottomSheetCallback() {
			override fun onStateChanged(bottomSheet: View, newState: Int) {
				backPressedCallback.isEnabled = newState != BottomSheetBehavior.STATE_COLLAPSED
			}

			override fun onSlide(bottomSheet: View, slideOffset: Float) {}
		})

		requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		filterViewModel.isFilterApplied.observe(viewLifecycleOwner) { onIsFilterAppliedChanged() }
		viewmodel.queryInfoResult.observe(viewLifecycleOwner, ::onQueryInfoResultChanged)

		adapter = PagedMediathekShowListAdapter(
			lifecycleScope,
			true,
			this@MediathekListFragment
		)

		binding.list.adapter = adapter.withLoadStateFooter(FooterLoadStateAdapter(adapter::retry))

		viewLifecycleOwner.launchOnCreated {
			viewmodel.pageFlow.collectLatest { pagingData ->
				adapter.submitData(pagingData)
			}
		}

		viewLifecycleOwner.launchOnCreated {
			viewmodel
				.pageFlow
				.drop(1)
				.collectLatest {
					// When new paging data was created we need to scroll up, because most
					// likely the filter parameters changed.
					// We skip the first item here, to not jump upong view recreation.
					binding.list.scrollToPosition(0)
				}
		}

		viewLifecycleOwner.launchOnCreated {
			adapter.loadStateFlow
				.map { it.refresh }
				.distinctUntilChanged()
				.collectLatest { refreshState ->
					binding.refreshLayout.isRefreshing = refreshState is LoadState.Loading
					binding.error.isVisible = refreshState is LoadState.Error
					updateNoShowsMessage(refreshState)

					when (refreshState) {
						is LoadState.Error -> onMediathekLoadErrorChanged(refreshState.error)
						is LoadState.NotLoading, LoadState.Loading -> Unit
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
		_noShowsBinding = null
		_bottomSheetBehavior = null
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.activity_main_toolbar, menu)
		menuInflater.inflate(R.menu.mediathek_list_fragment, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_filter -> {
				onFilterMenuClicked()
				true
			}

			R.id.menu_refresh -> {
				onRefresh()
				true
			}

			else -> false
		}
	}

	override fun onPrepareMenu(menu: Menu) {
		val filterIconResId = if (filterViewModel.isFilterApplied.value == true) {
			R.drawable.ic_sharp_filter_list_off_24
		} else {
			R.drawable.ic_sharp_filter_list_24
		}
		val filterItem = menu.findItem(R.id.menu_filter)
		filterItem.setIcon(filterIconResId)
	}

	override fun onShowClicked(show: MediathekShow) {
		val directions = MediathekListFragmentDirections.toMediathekDetailFragment(show)
		findNavController().navigate(directions)
	}

	override fun onShowLongClicked(show: MediathekShow, view: View) {
		ShowMenuHelper(this, show).apply {
			showContextMenu(view)
		}
	}

	override fun onRefresh() {
		adapter.refresh()
	}

	private fun onFilterMenuClicked() {
		if (filterViewModel.isFilterApplied.value == true) {
			filterViewModel.clearFilter()
		} else {
			toggleFilterBottomSheet()
		}
	}

	private fun onQueryInfoResultChanged(queryInfoResult: QueryInfoResult?) {
		filterViewModel.setQueryInfoResult(queryInfoResult)
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
		noShowsBinding.group.isVisible = isAdapterEmpty
	}
}
