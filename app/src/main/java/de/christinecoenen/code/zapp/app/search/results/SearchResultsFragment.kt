package de.christinecoenen.code.zapp.app.search.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.ui.helper.ShowMenuHelper
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragmentDirections
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowListItemListener
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.PagedMediathekShowListAdapter
import de.christinecoenen.code.zapp.app.personal.adapter.HeaderAdapater
import de.christinecoenen.code.zapp.app.search.SearchViewModel
import de.christinecoenen.code.zapp.databinding.SearchResultsFragmentBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnResumed
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SearchResultsFragment : Fragment(), MenuProvider, MediathekShowListItemListener {

	private var _binding: SearchResultsFragmentBinding? = null
	private val binding: SearchResultsFragmentBinding get() = _binding!!

	private val viewModel: SearchViewModel by activityViewModel()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = SearchResultsFragmentBinding.inflate(inflater, container, false)

		requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val adapter = ConcatAdapter()
		val localShowsResultAdapter = PagedMediathekShowListAdapter(
			lifecycleScope,
			true,
			this
		)
		val mediathekResultAdapter = PagedMediathekShowListAdapter(
			lifecycleScope,
			true,
			this
		)

		// TODO: hide header when there are no results
		adapter.addAdapter(
			HeaderAdapater(
				R.string.activity_main_tab_personal,
				R.drawable.ic_outline_app_shortcut_24,
				null
			)
		)
		adapter.addAdapter(localShowsResultAdapter)

		// TODO: hide header when there are no results
		adapter.addAdapter(
			HeaderAdapater(
				R.string.activity_main_tab_mediathek,
				R.drawable.ic_outline_video_library_24,
				null
			)
		)
		adapter.addAdapter(mediathekResultAdapter)

		binding.results.adapter = adapter

		viewLifecycleOwner.launchOnResumed {
			viewModel.localShowsResult.collectLatest { localShows ->
				localShowsResultAdapter.submitData(localShows)
			}
		}

		viewLifecycleOwner.launchOnResumed {
			viewModel.mediathekResult.collectLatest { apiShows ->
				mediathekResultAdapter.submitData(apiShows)
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.search_results_fragment, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_search -> {
				viewModel.enterQueryMode()
				true
			}

			else -> false
		}
	}

	override fun onShowClicked(show: MediathekShow) {
		val directions = SearchResultsFragmentDirections.toMediathekDetailFragment(show)
		findNavController().navigate(directions)
	}

	override fun onShowLongClicked(show: MediathekShow, view: View) {
		ShowMenuHelper(this, show).apply {
			showContextMenu(view)
		}
	}
}
