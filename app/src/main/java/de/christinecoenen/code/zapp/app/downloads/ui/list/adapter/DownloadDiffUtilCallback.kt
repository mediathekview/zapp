package de.christinecoenen.code.zapp.app.downloads.ui.list.adapter

import androidx.recyclerview.widget.DiffUtil
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow


class DownloadDiffUtilCallback : DiffUtil.ItemCallback<PersistedMediathekShow>() {

	override fun areItemsTheSame(
            old: PersistedMediathekShow,
            new: PersistedMediathekShow
	): Boolean = old.id == new.id

	/**
	 * We react to changed content via flows, so we only compare ids here.
	 */
	override fun areContentsTheSame(
            old: PersistedMediathekShow,
            new: PersistedMediathekShow
	): Boolean = old.id == new.id

}
