package de.christinecoenen.code.zapp.app.personal

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.ui.helper.ShowMenuHelper
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekItemType
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowListItemListener
import de.christinecoenen.code.zapp.app.personal.adapter.HeaderAdapater
import de.christinecoenen.code.zapp.app.personal.adapter.LoadStatusAdapter
import de.christinecoenen.code.zapp.app.personal.adapter.MediathekShowListAdapter
import de.christinecoenen.code.zapp.databinding.PersonalFragmentBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PersonalFragment : Fragment(), MenuProvider {

	private var _binding: PersonalFragmentBinding? = null
	private val binding: PersonalFragmentBinding get() = _binding!!

	private val viewModel: PersonalViewModel by viewModel()

	private lateinit var outerAdapter: ConcatAdapter
	private lateinit var downloadsAdapter: MediathekShowListAdapter
	private lateinit var continueWatchingAdapter: MediathekShowListAdapter
	private lateinit var bookmarkAdapter: MediathekShowListAdapter

	private val downloadsHeaderAdapter = HeaderAdapater(
		R.string.activity_main_tab_downloads,
		R.drawable.ic_baseline_save_alt_24,
	) { navigateToDownloads() }
	private val continueWatchingHeaderAdapter = HeaderAdapater(
		R.string.activity_main_tab_continue_watching,
		R.drawable.ic_outline_play_circle_24
	) { navigateToContinueWatching() }
	private val bookmarksHeaderAdapter = HeaderAdapater(
		R.string.activity_main_tab_bookmarks,
		R.drawable.ic_outline_bookmarks_24
	) { navigateToBookmarks() }

	private val downloadsLoadStatusAdapter = LoadStatusAdapter()
	private val continueWatchingLoadStatusAdapter = LoadStatusAdapter()
	private val bookmarkLoadStatusAdapter = LoadStatusAdapter()

	private val showClickListener = object : MediathekShowListItemListener {
		override fun onShowClicked(show: MediathekShow) {
			navigateToShow(show)
		}

		override fun onShowLongClicked(show: MediathekShow, view: View) {
			ShowMenuHelper(this@PersonalFragment, show).apply {
				showContextMenu(view)
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		downloadsAdapter =
			MediathekShowListAdapter(lifecycleScope, MediathekItemType.Download, showClickListener)
		continueWatchingAdapter =
			MediathekShowListAdapter(
				lifecycleScope,
				MediathekItemType.ContinueWatching,
				showClickListener
			)
		bookmarkAdapter =
			MediathekShowListAdapter(lifecycleScope, MediathekItemType.Bookmark, showClickListener)

		outerAdapter = ConcatAdapter(
			downloadsHeaderAdapter,
			downloadsAdapter,
			downloadsLoadStatusAdapter,
			continueWatchingHeaderAdapter,
			continueWatchingAdapter,
			continueWatchingLoadStatusAdapter,
			bookmarksHeaderAdapter,
			bookmarkAdapter,
			bookmarkLoadStatusAdapter,
		)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = PersonalFragmentBinding.inflate(inflater, container, false)

		binding.list.adapter = outerAdapter

		lifecycleScope.launchWhenCreated {
			viewModel.downloadsFlow.collect {
				downloadsAdapter.setShows(it)
				downloadsHeaderAdapter.setShowMoreButton(it.isNotEmpty())
				downloadsLoadStatusAdapter.onShowsLoaded(it.size)
			}
		}

		lifecycleScope.launchWhenCreated {
			viewModel.continueWatchingFlow.collect {
				continueWatchingAdapter.setShows(it)
				continueWatchingHeaderAdapter.setShowMoreButton(it.isNotEmpty())
				continueWatchingLoadStatusAdapter.onShowsLoaded(it.size)
			}
		}

		lifecycleScope.launchWhenCreated {
			viewModel.bookmarkFlow.collect {
				bookmarkAdapter.setShows(it)
				bookmarksHeaderAdapter.setShowMoreButton(it.isNotEmpty())
				bookmarkLoadStatusAdapter.onShowsLoaded(it.size)
			}
		}

		requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.activity_main_toolbar, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false

	private fun navigateToDownloads() {
		val directions = PersonalFragmentDirections.toDownloadsFragment()
		findNavController().navigate(directions)
	}

	private fun navigateToContinueWatching() {
		val directions = PersonalFragmentDirections.toContinueWatchingFragment()
		findNavController().navigate(directions)
	}

	private fun navigateToBookmarks() {
		val directions = PersonalFragmentDirections.toBookmarksFragment()
		findNavController().navigate(directions)
	}

	private fun navigateToShow(show: MediathekShow) {
		val directions =
			PersonalFragmentDirections.toMediathekDetailFragment(mediathekShow = show)
		findNavController().navigate(directions)
	}
}
