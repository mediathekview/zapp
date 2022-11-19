package de.christinecoenen.code.zapp.utils.view

import androidx.recyclerview.widget.DiffUtil
import de.christinecoenen.code.zapp.models.shows.MediathekShow


class MediathekShowDiffUtilItemCallback : DiffUtil.ItemCallback<MediathekShow>() {

	override fun areItemsTheSame(
		old: MediathekShow,
		new: MediathekShow
	): Boolean = old.apiId == new.apiId

	/**
	 * We react to changed content via flows, so we only compare ids here.
	 */
	override fun areContentsTheSame(
		old: MediathekShow,
		new: MediathekShow
	): Boolean = old.apiId == new.apiId

}
