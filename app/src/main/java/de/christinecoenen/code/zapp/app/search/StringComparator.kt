package de.christinecoenen.code.zapp.app.search

import androidx.recyclerview.widget.DiffUtil

object StringComparator : DiffUtil.ItemCallback<String>() {
	override fun areItemsTheSame(
		oldItem: String,
		newItem: String
	): Boolean {
		return oldItem == newItem
	}

	override fun areContentsTheSame(
		oldItem: String,
		newItem: String
	) = areItemsTheSame(oldItem, newItem)
}
