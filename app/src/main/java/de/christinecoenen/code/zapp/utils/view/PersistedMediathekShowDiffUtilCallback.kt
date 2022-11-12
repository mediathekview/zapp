package de.christinecoenen.code.zapp.utils.view

import androidx.recyclerview.widget.DiffUtil
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow

class PersistedMediathekShowDiffUtilCallback(
	private val oldShows: List<PersistedMediathekShow>,
	private val newShows: List<PersistedMediathekShow>
) : DiffUtil.Callback() {

	private val itemCallback = PersistedMediathekShowDiffUtilItemCallback()

	override fun getOldListSize() = oldShows.size

	override fun getNewListSize() = newShows.size

	override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
		return itemCallback.areItemsTheSame(oldShows[oldItemPosition], newShows[newItemPosition])
	}

	override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
		return itemCallback.areContentsTheSame(oldShows[oldItemPosition], newShows[newItemPosition])
	}
}
