package de.christinecoenen.code.zapp.app.search.suggestions.chips

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.SearchChipBinding

class ChipViewHolder(
	val binding: SearchChipBinding,
	private val type: ChipType
) :
	RecyclerView.ViewHolder(binding.root) {

	fun setContent(content: ChipContent) {
		binding.root.isCheckable = type !== ChipType.Suggestion
		binding.root.isChecked = type !== ChipType.Suggestion
		binding.root.isCloseIconVisible = type === ChipType.InteractableFilter
		binding.root.isChipIconVisible = type === ChipType.Suggestion

		if (type === ChipType.NonInteractableFilter) {
			binding.root.setOnCheckedChangeListener { compoundButton, _ ->
				compoundButton.isChecked = true
			}
		}

		binding.root.text = content.label
	}
}
