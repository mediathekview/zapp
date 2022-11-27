package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import androidx.recyclerview.widget.DiffUtil

object UiModelComparator : DiffUtil.ItemCallback<UiModel>() {
	override fun areItemsTheSame(
        oldItem: UiModel,
        newItem: UiModel
	): Boolean {
		val isSameRepoItem = oldItem is UiModel.MediathekShowModel
			&& newItem is UiModel.MediathekShowModel
			&& oldItem.show.apiId == newItem.show.apiId

		val isSameSeparatorItem = oldItem is UiModel.DateSeparatorModel
			&& newItem is UiModel.DateSeparatorModel
			&& newItem.date == oldItem.date

		return isSameRepoItem || isSameSeparatorItem
	}

	override fun areContentsTheSame(
        oldItem: UiModel,
        newItem: UiModel
	) = areItemsTheSame(oldItem, newItem)
}
