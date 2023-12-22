package de.christinecoenen.code.zapp.app.search.suggestions.chips

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.SearchChipBinding

class ChipViewHolder(
	val binding: SearchChipBinding,
	private val type: ChipType
) :
	RecyclerView.ViewHolder(binding.root) {

	fun setContent(content: ChipContent) {
		binding.root.isCheckable = type === ChipType.Filter
		binding.root.isChecked = type === ChipType.Filter
		binding.root.isCloseIconVisible = type === ChipType.Filter
		binding.root.isChipIconVisible = type === ChipType.Suggestion

		binding.root.text = content.label
	}
}
