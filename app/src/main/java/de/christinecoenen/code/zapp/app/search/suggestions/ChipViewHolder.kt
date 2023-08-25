package de.christinecoenen.code.zapp.app.search.suggestions

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.SearchChipBinding

class ChipViewHolder(val binding: SearchChipBinding) :
	RecyclerView.ViewHolder(binding.root) {

	fun setText(text: String) {
		binding.root.text = text
	}
}
