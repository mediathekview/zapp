package de.christinecoenen.code.zapp.app.search.suggestions.text

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.SearchSuggestionClearHistoryButtonBinding

class ClearSearchHistoryButtonAdapter(
	private val listener: Listener
) : RecyclerView.Adapter<ClearSearchHistoryButtonAdapter.ViewHolder>() {

	private var isVisible = false

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
		val binding =
			SearchSuggestionClearHistoryButtonBinding.inflate(layoutInflater, parent, false)

		binding.clearAllButton.setOnClickListener {
			listener.onClearSearchHistoryClicked()
		}

		return ViewHolder(binding)
	}

	override fun onBindViewHolder(
		holder: ViewHolder,
		position: Int
	) {
	}

	override fun getItemCount() = if (isVisible) 1 else 0

	inner class ViewHolder(
		binding: SearchSuggestionClearHistoryButtonBinding
	) : RecyclerView.ViewHolder(binding.root)

	interface Listener {
		fun onClearSearchHistoryClicked()
	}
}
