package de.christinecoenen.code.zapp.app.search.suggestions.text

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.SearchSuggestionItemHeaderBinding

class LocalSearchSuggestionsHeaderAdapter(
	@StringRes private val labelResId: Int
) :
	RecyclerView.Adapter<LocalSearchSuggestionsHeaderAdapter.ViewHolder>() {

	private var isVisible = true

	fun setIsVisible(isVisible: Boolean) {
		if (this.isVisible == isVisible) {
			return
		}

		this.isVisible = isVisible

		if (isVisible) {
			notifyItemInserted(0)
		} else {
			notifyItemRemoved(0)
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = SearchSuggestionItemHeaderBinding.inflate(layoutInflater, parent, false)

		return ViewHolder(binding)
	}

	override fun onBindViewHolder(
		holder: ViewHolder,
		position: Int
	) {
		holder.bind()
	}

	override fun getItemCount() = if (isVisible) 1 else 0

	inner class ViewHolder(
		private val binding: SearchSuggestionItemHeaderBinding
	) : RecyclerView.ViewHolder(binding.root) {
		fun bind() {
			binding.label.text =
				binding.root.resources.getString(this@LocalSearchSuggestionsHeaderAdapter.labelResId)
		}
	}
}
