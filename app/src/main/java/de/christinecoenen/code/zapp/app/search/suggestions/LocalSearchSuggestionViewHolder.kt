package de.christinecoenen.code.zapp.app.search.suggestions

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.SearchSuggestionItemLocalBinding

class LocalSearchSuggestionViewHolder(
	private val binding: SearchSuggestionItemLocalBinding
) : RecyclerView.ViewHolder(binding.root) {

	fun setSuggestion(suggestion: String) {
		binding.suggestion.text = suggestion
	}

}
