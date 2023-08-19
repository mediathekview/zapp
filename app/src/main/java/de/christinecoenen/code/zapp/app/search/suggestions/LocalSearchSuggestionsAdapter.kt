package de.christinecoenen.code.zapp.app.search.suggestions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import de.christinecoenen.code.zapp.app.search.StringComparator
import de.christinecoenen.code.zapp.databinding.SearchSuggestionItemLocalBinding

class LocalSearchSuggestionsAdapter(private val listener: Listener) :
	PagingDataAdapter<String, LocalSearchSuggestionViewHolder>(StringComparator) {
	override fun onBindViewHolder(holder: LocalSearchSuggestionViewHolder, position: Int) {
		holder.setSuggestion(getItem(position)!!)
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): LocalSearchSuggestionViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = SearchSuggestionItemLocalBinding.inflate(layoutInflater, parent, false)
		val holder = LocalSearchSuggestionViewHolder(binding)

		binding.root.setOnClickListener {
			listener.onSuggestionClicked(getItem(holder.bindingAdapterPosition)!!)
		}

		return holder
	}

	interface Listener {
		fun onSuggestionClicked(suggestion: String)
	}
}
