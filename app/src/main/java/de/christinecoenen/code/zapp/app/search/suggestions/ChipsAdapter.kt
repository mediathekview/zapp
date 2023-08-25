package de.christinecoenen.code.zapp.app.search.suggestions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import de.christinecoenen.code.zapp.databinding.SearchChipBinding

class ChipsAdapter : ListAdapter<String, ChipViewHolder>(Differ) {

	companion object {
		val Differ = object : DiffUtil.ItemCallback<String>() {
			override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
			override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = SearchChipBinding.inflate(layoutInflater, parent, false)
		return ChipViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
		holder.setText(getItem(position))
	}
}
