package de.christinecoenen.code.zapp.app.downloads.ui

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentListItemBinding


class DownloadViewHolder(val binding: DownloadsFragmentListItemBinding) :
	RecyclerView.ViewHolder(binding.root) {

	fun bindItem(show: PersistedMediathekShow) {
		binding.title.text = show.mediathekShow.title
	}

}
