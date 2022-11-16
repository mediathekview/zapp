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
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekItemType
import de.christinecoenen.code.zapp.app.personal.adapter.MediathekShowListAdapter
import de.christinecoenen.code.zapp.app.personal.adapter.HeaderAdapater
import de.christinecoenen.code.zapp.databinding.PersonalFragmentBinding
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PersonalFragment : Fragment(), MenuProvider {

	private var _binding: PersonalFragmentBinding? = null
	private val binding: PersonalFragmentBinding get() = _binding!!

	private val viewModel: PersonalViewModel by viewModel()

	private lateinit var outerAdapter: ConcatAdapter
	private lateinit var downloadsAdapter: MediathekShowListAdapter
	private lateinit var historyAdapter: MediathekShowListAdapter
	private lateinit var bookmarkAdapter: MediathekShowListAdapter

	private val showClickListener = object : MediathekShowListAdapter.Listener {
		override fun onShowClicked(show: PersistedMediathekShow) {
			navigateToShow(show)
		}

		override fun onShowLongClicked(show: PersistedMediathekShow, view: View) {
			// TODO: implement
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		downloadsAdapter =
			MediathekShowListAdapter(lifecycleScope, MediathekItemType.Download, showClickListener)
		historyAdapter =
			MediathekShowListAdapter(lifecycleScope, MediathekItemType.History, showClickListener)
		bookmarkAdapter =
			MediathekShowListAdapter(lifecycleScope, MediathekItemType.Bookmark, showClickListener)

		outerAdapter = ConcatAdapter(
			HeaderAdapater(
				R.string.activity_main_tab_downloads,
				R.drawable.ic_baseline_save_alt_24
			) { navigateToDownloads() },
			downloadsAdapter,
			HeaderAdapater(
				R.string.activity_main_tab_history,
				R.drawable.ic_outline_play_circle_24,
				null
			),
			historyAdapter,
			HeaderAdapater(
				R.string.activity_main_tab_bookmarks,
				R.drawable.ic_baseline_bookmark_border_24,
				null
			),
			bookmarkAdapter,
		)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = PersonalFragmentBinding.inflate(inflater, container, false)

		binding.list.adapter = outerAdapter

		lifecycleScope.launch {
			viewModel.downloadsFlow.collect {
				downloadsAdapter.setShows(it)
			}
		}

		lifecycleScope.launch {
			viewModel.historyFlow.collect {
				historyAdapter.setShows(it)
			}
		}

		lifecycleScope.launch {
			viewModel.bookmarkFlow.collect {
				bookmarkAdapter.setShows(it)
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

	private fun navigateToShow(show: PersistedMediathekShow) {
		val directions =
			PersonalFragmentDirections.toMediathekDetailFragment(persistedShowId = show.id)
		findNavController().navigate(directions)
	}
}
