package de.christinecoenen.code.zapp.app.search.suggestions

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.SearchChipBinding

class ChipViewHolder(
	val binding: SearchChipBinding,
	private val type: ChipsAdapter.Type
) :
	RecyclerView.ViewHolder(binding.root) {

	fun setContent(content: ChipsAdapter.ChipContent) {
		binding.root.isCheckable = type === ChipsAdapter.Type.Filter
		binding.root.isChecked = type === ChipsAdapter.Type.Filter
		binding.root.isCloseIconVisible = type === ChipsAdapter.Type.Filter
		binding.root.isChipIconVisible = type === ChipsAdapter.Type.Suggestion

		binding.root.text = content.label
	}
}
