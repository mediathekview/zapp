package de.christinecoenen.code.zapp.app.search.suggestions.chips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import de.christinecoenen.code.zapp.databinding.SearchChipBinding

class ChipsAdapter<T : ChipContent>(
	private val type: ChipType,
	private val listener: SuggestionChipListener<T>? = null
) :
	ListAdapter<T, ChipViewHolder>(getDiffer()) {

	companion object {
		fun <T : ChipContent> getDiffer(): DiffUtil.ItemCallback<T> {
			return object : DiffUtil.ItemCallback<T>() {
				override fun areItemsTheSame(oldItem: T, newItem: T) =
					oldItem.content == newItem.content

				override fun areContentsTheSame(oldItem: T, newItem: T) =
					oldItem.label == newItem.label
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = SearchChipBinding.inflate(layoutInflater, parent, false)
		val holder = ChipViewHolder(binding, type)

		binding.root.setOnClickListener {
			listener?.onChipClick(getItem(holder.bindingAdapterPosition))
		}

		return holder
	}

	override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
		holder.setContent(getItem(position))
	}

}
