package de.christinecoenen.code.zapp.tv.mediathek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.leanback.widget.SearchBar
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragmentViewModel
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.FooterLoadStateAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowComparator
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.databinding.TvFragmentMediathekListBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.tv.player.PlayerActivity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MediathekListFragment : Fragment(),
	de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.ListItemListener,
	SearchBar.SearchBarListener {

	private val mediathekRepository: MediathekRepository by inject()

	private val viewmodel: MediathekListFragmentViewModel by viewModel()
	private lateinit var adapter: MediathekItemAdapter

	private var _binding: TvFragmentMediathekListBinding? = null
	private val binding: TvFragmentMediathekListBinding
		get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = TvFragmentMediathekListBinding.inflate(inflater, container, false)

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

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewLifecycleOwner.lifecycleScope.launch {
			adapter.loadStateFlow
				.drop(1)
				.map { it.refresh }
				.distinctUntilChanged()
				.collectLatest { refreshState ->
					binding.loader.isVisible = refreshState is LoadState.Loading
					//binding.error.isVisible = refreshState is LoadState.Error
					//updateNoShowsMessage(refreshState)

					when (refreshState) {
						//is LoadState.Error -> onMediathekLoadErrorChanged(refreshState.error)
						is LoadState.NotLoading -> binding.list.scrollToPosition(0)
						is LoadState.Loading -> Unit
					}
				}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		_binding = null
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
		viewmodel.setSearchQueryFilter(query)
	}

	override fun onSearchQuerySubmit(query: String?) {

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
