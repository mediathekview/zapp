package de.christinecoenen.code.zapp.app.downloads.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import de.christinecoenen.code.zapp.app.ZappApplicationBase
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.MediathekDetailActivity
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentBinding

class DownloadsFragment : Fragment(), DownloadListAdapter.Listener {

	companion object {
		fun newInstance() = DownloadsFragment()
	}


	private var _binding: DownloadsFragmentBinding? = null
	private val binding: DownloadsFragmentBinding get() = _binding!!

	private val viewModel: DownloadsViewModel by viewModels {
		val application = requireContext().applicationContext as ZappApplicationBase
		DownloadsViewModelFactory(application.mediathekRepository)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		_binding = DownloadsFragmentBinding.inflate(inflater, container, false)

		val downloadAdapter = DownloadListAdapter(this, viewModel)
		binding.list.adapter = downloadAdapter

		viewModel.downloadList.observe(viewLifecycleOwner) { downloadAdapter.submitList(it) }

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onShowClicked(show: PersistedMediathekShow) {
		startActivity(MediathekDetailActivity.getStartIntent(context, show.mediathekShow))
	}
}
