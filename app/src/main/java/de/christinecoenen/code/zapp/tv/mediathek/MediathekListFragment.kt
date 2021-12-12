package de.christinecoenen.code.zapp.tv.mediathek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.leanback.widget.SearchBar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragmentViewModel
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.FooterLoadStateAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowComparator
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.databinding.TvFragmentMediathekListBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.tv.player.PlayerActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MediathekListFragment : Fragment(),
	de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.ListItemListener,
	SearchBar.SearchBarListener {

	private val mediathekRepository: MediathekRepository by inject()

	private val viewmodel: MediathekListFragmentViewModel by viewModel()
	private lateinit var adapter: MediathekItemAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val binding = TvFragmentMediathekListBinding.inflate(inflater, container, false)

		val layoutManager = LinearLayoutManager(binding.root.context)
		binding.list.layoutManager = layoutManager

		binding.search.setSearchBarListener(this)

		adapter = MediathekItemAdapter(MediathekShowComparator, this)
		binding.list.adapter = adapter.withLoadStateFooter(FooterLoadStateAdapter(adapter::retry))

		viewLifecycleOwner.lifecycleScope.launch {
			viewmodel.pageFlow.collectLatest { pagingData ->
				adapter.submitData(pagingData)
			}
		}

		return binding.root
	}

	override fun onShowClicked(show: MediathekShow) {
		lifecycleScope.launchWhenResumed {
			saveAndOpenShow(show)
		}
	}

	override fun onShowLongClicked(show: MediathekShow, view: View) {
		// no action
	}

	override fun onSearchQueryChange(query: String?) {

	}

	override fun onSearchQuerySubmit(query: String?) {
		viewmodel.setSearchQueryFilter(query)
	}

	override fun onKeyboardDismiss(query: String?) {

	}

	private suspend fun saveAndOpenShow(show: MediathekShow) {
		val persistedShow = mediathekRepository.persistOrUpdateShow(show).first()

		val videoInfo = VideoInfo.fromShow(persistedShow)
		val intent = PlayerActivity.getStartIntent(requireContext(), videoInfo)
		startActivity(intent)
	}
}
