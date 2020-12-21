package de.christinecoenen.code.zapp.app.downloads.ui

import androidx.recyclerview.widget.DiffUtil
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow


class DownloadDiffUtilCallback : DiffUtil.ItemCallback<PersistedMediathekShow>() {

	override fun areItemsTheSame(
		old: PersistedMediathekShow,
		new: PersistedMediathekShow
	): Boolean = old.id == new.id

	override fun areContentsTheSame(
		old: PersistedMediathekShow,
		new: PersistedMediathekShow
	): Boolean = old == new

}
