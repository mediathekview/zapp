package de.christinecoenen.code.zapp.app.downloads.ui.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import de.christinecoenen.code.zapp.app.downloads.ui.list.DownloadsViewModel
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentListItemBinding


class DownloadListAdapter(
        private val listener: Listener,
        private val downloadsViewModel: DownloadsViewModel
) : PagedListAdapter<PersistedMediathekShow, DownloadViewHolder>(DownloadDiffUtilCallback()) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = DownloadsFragmentListItemBinding.inflate(layoutInflater, parent, false)
		val holder = DownloadViewHolder(binding)

		binding.root.setOnClickListener {
			getItem(holder.adapterPosition)?.let {
				listener.onShowClicked(it)
			}
		}

		return holder
	}

	override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
		getItem(position)?.let {
			val showFlowable = downloadsViewModel.getPersistedShowFlowable(it.id)
			holder.bindItem(it, showFlowable)
		}
	}

	interface Listener {
		fun onShowClicked(show: PersistedMediathekShow)
	}
}
