package de.christinecoenen.code.zapp.utils.view

import androidx.recyclerview.widget.DiffUtil
import de.christinecoenen.code.zapp.models.shows.MediathekShow

class MediathekShowDiffUtilCallback(
	private val oldShows: List<MediathekShow>,
	private val newShows: List<MediathekShow>
) : DiffUtil.Callback() {

	private val itemCallback = MediathekShowDiffUtilItemCallback()

	override fun getOldListSize() = oldShows.size

	override fun getNewListSize() = newShows.size

	override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
		return itemCallback.areItemsTheSame(oldShows[oldItemPosition], newShows[newItemPosition])
	}

	override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
		return itemCallback.areContentsTheSame(oldShows[oldItemPosition], newShows[newItemPosition])
	}
}
