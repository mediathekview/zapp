package de.christinecoenen.code.zapp.app.search.suggestions.text

import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.SearchSuggestionItemLocalBinding

class LocalSearchSuggestionViewHolder(
	private val binding: SearchSuggestionItemLocalBinding,
	@DrawableRes
	private val typeIcon: Int,
) : RecyclerView.ViewHolder(binding.root) {

	fun setSuggestion(suggestion: String) {
		binding.typeIcon.setImageResource(typeIcon)
		binding.suggestion.text = suggestion
	}

}
