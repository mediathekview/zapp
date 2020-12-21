package de.christinecoenen.code.zapp.app.downloads.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentListItemBinding


class DownloadListAdapter : PagedListAdapter<PersistedMediathekShow, DownloadViewHolder>(DownloadDiffUtilCallback()) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = DownloadsFragmentListItemBinding.inflate(layoutInflater, parent, false)
		return DownloadViewHolder(binding)
	}

	override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
		getItem(position)?.let {
			holder.bindItem(it)
		}
	}

}
