package de.christinecoenen.code.zapp.app.search.suggestions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.paging.PagingDataAdapter
import de.christinecoenen.code.zapp.app.search.StringComparator
import de.christinecoenen.code.zapp.databinding.SearchSuggestionItemLocalBinding

class LocalSearchSuggestionsAdapter(
	private val listener: Listener,
	@DrawableRes
	private val typeIcon: Int,
) :
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
		val holder = LocalSearchSuggestionViewHolder(binding, typeIcon)

		binding.root.setOnClickListener {
			listener.onSuggestionSelected(getItem(holder.bindingAdapterPosition)!!)
		}
		binding.insertButton.setOnClickListener {
			listener.onSuggestionInserted(getItem(holder.bindingAdapterPosition)!!)
		}

		return holder
	}

	interface Listener {
		fun onSuggestionSelected(suggestion: String)
		fun onSuggestionInserted(suggestion: String)
	}
}
