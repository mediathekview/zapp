package de.christinecoenen.code.zapp.app.downloads.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.app.downloads.ui.list.adapter.DownloadListAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.MediathekDetailActivity
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentBinding
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import org.koin.androidx.viewmodel.ext.android.viewModel

class DownloadsFragment : Fragment(), DownloadListAdapter.Listener {

	companion object {
		fun newInstance() = DownloadsFragment()
	}


	private var _binding: DownloadsFragmentBinding? = null
	private val binding: DownloadsFragmentBinding get() = _binding!!

	private val viewModel: DownloadsViewModel by viewModel()
	private lateinit var downloadAdapter: DownloadListAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DownloadsFragmentBinding.inflate(inflater, container, false)

		downloadAdapter = DownloadListAdapter(this, viewModel)
		binding.list.adapter = downloadAdapter

		downloadAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
			override fun onChanged() {
				updateNoDownloadsVisibility()
			}
		})

		viewModel.downloadList.observe(viewLifecycleOwner) {
			lifecycleScope.launchWhenCreated {
				downloadAdapter.submitData(it)
			}
		}

		updateNoDownloadsVisibility()

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onShowClicked(show: PersistedMediathekShow) {
		startActivity(MediathekDetailActivity.getStartIntent(context, show.mediathekShow))
	}

	private fun updateNoDownloadsVisibility() {
		binding.noDownloads.isVisible = downloadAdapter.itemCount == 0
	}
}
