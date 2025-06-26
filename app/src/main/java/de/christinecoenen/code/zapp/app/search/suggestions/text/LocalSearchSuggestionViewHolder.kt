package de.christinecoenen.code.zapp.app.search.suggestions.text

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.SearchSuggestionItemLocalBinding

class LocalSearchSuggestionViewHolder(
	private val binding: SearchSuggestionItemLocalBinding,
	private val type: TextSuggestionType,
) : RecyclerView.ViewHolder(binding.root) {

	fun setSuggestion(suggestion: String) {
		binding.typeIcon.setImageResource(type.iconResId)
		binding.suggestion.text = suggestion
	}

}
