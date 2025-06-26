package de.christinecoenen.code.zapp.app.search.suggestions.text

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import de.christinecoenen.code.zapp.app.search.StringComparator
import de.christinecoenen.code.zapp.databinding.SearchSuggestionItemLocalBinding

class LocalSearchSuggestionsAdapter(
	private val listener: SuggestionTextListener,
	private val type: TextSuggestionType,
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
		val holder = LocalSearchSuggestionViewHolder(binding, type)

		binding.root.setOnClickListener {
			listener.onSuggestionSelected(getItem(holder.bindingAdapterPosition)!!)
		}
		binding.root.setOnLongClickListener {
			listener.onSuggestionLongPress(
				getItem(holder.bindingAdapterPosition)!!,
				type,
				binding.root
			)
		}
		binding.insertButton.setOnClickListener {
			listener.onSuggestionInserted(getItem(holder.bindingAdapterPosition)!!)
		}

		return holder
	}

}
