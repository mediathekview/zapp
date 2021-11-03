package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import androidx.recyclerview.widget.DiffUtil
import de.christinecoenen.code.zapp.models.shows.MediathekShow

object MediathekShowComparator : DiffUtil.ItemCallback<MediathekShow>() {

	override fun areItemsTheSame(oldItem: MediathekShow, newItem: MediathekShow): Boolean {
		return oldItem.apiId == newItem.apiId
	}

	override fun areContentsTheSame(oldItem: MediathekShow, newItem: MediathekShow): Boolean {
		return oldItem == newItem
	}

}
