package de.christinecoenen.code.zapp.app.downloads.ui

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentListItemBinding


class DownloadViewHolder(val binding: DownloadsFragmentListItemBinding) :
	RecyclerView.ViewHolder(binding.root) {

	fun bindItem(show: PersistedMediathekShow) {
		binding.topic.text = show.mediathekShow.topic
		binding.title.text = show.mediathekShow.title
		binding.duration.text = show.mediathekShow.formattedDuration
		binding.channel.text = show.mediathekShow.channel
		binding.time.text = show.mediathekShow.formattedTimestamp
	}

}
