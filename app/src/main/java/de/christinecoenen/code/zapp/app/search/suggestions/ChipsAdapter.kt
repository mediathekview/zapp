package de.christinecoenen.code.zapp.app.search.suggestions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import de.christinecoenen.code.zapp.app.mediathek.api.request.MediathekChannel
import de.christinecoenen.code.zapp.databinding.SearchChipBinding

class ChipsAdapter<T : ChipsAdapter.ChipContent>(
	private val type: Type,
	private val listener: Listener<T>
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
			listener.onChipClick(getItem(holder.bindingAdapterPosition))
		}

		return holder
	}

	override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
		holder.setContent(getItem(position))
	}

	interface ChipContent {
		val content: Any
		val label: String
	}

	data class ChannelChipContent(
		val channel: MediathekChannel,
	) : ChipContent {
		override val content = channel
		override val label = channel.apiId
	}

	interface Listener<T : ChipContent> {
		fun onChipClick(content: T)
	}

	enum class Type {
		Filter,
		Suggestion
	}
}
