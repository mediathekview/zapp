package de.christinecoenen.code.zapp.tv.mediathek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragmentViewModel
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.FooterLoadStateAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowComparator
import de.christinecoenen.code.zapp.databinding.TvFragmentMediathekListBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MediathekListFragment : Fragment(),
	de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.ListItemListener {

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
		TODO("Not yet implemented")
	}

	override fun onShowLongClicked(show: MediathekShow, view: View) {
		TODO("Not yet implemented")
	}
}
