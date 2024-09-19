package de.christinecoenen.code.zapp.app.search.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.ui.helper.ShowMenuHelper
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekLoadStateAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowListItemListener
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.PagedMediathekShowListAdapter
import de.christinecoenen.code.zapp.app.personal.adapter.HeaderAdapater
import de.christinecoenen.code.zapp.app.personal.adapter.LoadStatusAdapter
import de.christinecoenen.code.zapp.app.search.SearchViewModel
import de.christinecoenen.code.zapp.app.search.suggestions.chips.ChannelChipContent
import de.christinecoenen.code.zapp.app.search.suggestions.chips.ChipType
import de.christinecoenen.code.zapp.app.search.suggestions.chips.ChipsAdapter
import de.christinecoenen.code.zapp.app.search.suggestions.chips.DurationChipContent
import de.christinecoenen.code.zapp.databinding.SearchResultsFragmentBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnResumed
import de.christinecoenen.code.zapp.utils.system.SystemUiHelper.applyBottomInsetAsPadding
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SearchResultsFragment : Fragment(), MediathekShowListItemListener {

	private var _binding: SearchResultsFragmentBinding? = null
	private val binding: SearchResultsFragmentBinding get() = _binding!!

	private val viewModel: SearchViewModel by activityViewModel()

	private val localShowsResultHeaderAdapater = HeaderAdapater(
		R.string.activity_main_tab_personal,
		R.drawable.ic_outline_app_shortcut_24,
		null
	).apply { setIsVisible(false) }
	private val mediathekResultHeaderAdapter = HeaderAdapater(
		R.string.activity_main_tab_mediathek,
		R.drawable.ic_outline_video_library_24,
		null
	)

	private val mediathekResultLoadStatusAdapter =
		LoadStatusAdapter(R.string.fragment_mediathek_no_results)

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = SearchResultsFragmentBinding.inflate(inflater, container, false)

		binding.results.applyBottomInsetAsPadding()

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

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

		val channelChipsAdapter = ChipsAdapter<ChannelChipContent>(ChipType.NonInteractableFilter)
		viewLifecycleOwner.launchOnResumed {
			viewModel.channels.collectLatest { channels ->
				channelChipsAdapter.submitList(channels.map {
					ChannelChipContent(it)
				})
			}
		}

		val durationChipsAdapter = ChipsAdapter<DurationChipContent>(ChipType.NonInteractableFilter)
		viewLifecycleOwner.launchOnResumed {
			viewModel.durationQuerySet.collectLatest { durationQuerySet ->
				durationChipsAdapter.submitList(durationQuerySet.map {
					DurationChipContent(it)
				})
			}
		}

		binding.chips.adapter = ConcatAdapter(durationChipsAdapter, channelChipsAdapter)

		val mediathekResultLoadStateHeader =
			MediathekLoadStateAdapter(retry = mediathekResultAdapter::retry)
		val showsAdapter = ConcatAdapter(
			localShowsResultHeaderAdapater,
			localShowsResultAdapter,
			mediathekResultHeaderAdapter,
			mediathekResultLoadStateHeader,
			mediathekResultAdapter
				.withLoadStateFooter(MediathekLoadStateAdapter(retry = mediathekResultAdapter::retry)),
			mediathekResultLoadStatusAdapter
		)
		binding.results.adapter = showsAdapter

		viewLifecycleOwner.launchOnResumed {
			viewModel.localShowsResult.collectLatest { localShows ->
				localShowsResultAdapter.submitData(localShows)
			}
		}
		localShowsResultAdapter.addOnPagesUpdatedListener {
			localShowsResultHeaderAdapater.setIsVisible(localShowsResultAdapter.itemCount > 0)
		}

		viewLifecycleOwner.launchOnResumed {
			viewModel.mediathekResult.collectLatest { apiShows ->
				mediathekResultAdapter.submitData(apiShows)
			}
		}
		mediathekResultAdapter.addLoadStateListener { loadStates ->
			// show initial load, too
			mediathekResultLoadStateHeader.loadState = loadStates.refresh
		}
		mediathekResultAdapter.addOnPagesUpdatedListener {
			mediathekResultLoadStatusAdapter.onShowsLoaded(mediathekResultAdapter.itemCount)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
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
