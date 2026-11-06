package de.christinecoenen.code.zapp.app.personal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.PersonalFragmentSeriesItemBinding
import de.christinecoenen.code.zapp.models.collections.ShowCollection
import de.christinecoenen.code.zapp.models.collections.channelLabels
import de.christinecoenen.code.zapp.models.collections.excludedSearchTerms
import de.christinecoenen.code.zapp.models.collections.selectedChannels

class ShowCollectionListAdapter(
	private val listener: Listener,
) : ListAdapter<ShowCollection, ShowCollectionListAdapter.ViewHolder>(DiffCallback) {

	companion object {

		private val DiffCallback = object : DiffUtil.ItemCallback<ShowCollection>() {
			override fun areItemsTheSame(
				oldItem: ShowCollection,
				newItem: ShowCollection
			) = oldItem.id == newItem.id

			override fun areContentsTheSame(
				oldItem: ShowCollection,
				newItem: ShowCollection
			) = oldItem == newItem
		}
	}

	interface Listener {
		fun onCollectionClicked(collection: ShowCollection)

		fun onCollectionMenuClicked(collection: ShowCollection, view: View)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = PersonalFragmentSeriesItemBinding.inflate(layoutInflater, parent, false)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position), listener)
	}

	class ViewHolder(
		private val binding: PersonalFragmentSeriesItemBinding,
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(collection: ShowCollection, listener: Listener) {
			val context = binding.root.context

			binding.name.text = collection.name

			val details = mutableListOf(
				context.getString(R.string.fragment_series_item_query, collection.searchQuery)
			)
			if (collection.excludedSearchTerms.isNotEmpty()) {
				details += context.getString(
					R.string.fragment_series_item_excluded,
					collection.excludeTerms
				)
			}
			if (collection.selectedChannels.isNotEmpty()) {
				details += context.getString(
					R.string.fragment_series_item_channels,
					collection.channelLabels.joinToString(", ")
				)
			}
			binding.query.text = details.joinToString(" · ")

			val hasCount = collection.countUpdatedAt != null
			binding.count.isVisible = hasCount
			if (hasCount) {
				binding.count.text = binding.root.context.getString(
					R.string.fragment_series_item_count,
					collection.unwatchedCount
				)
				binding.count.contentDescription = binding.root.context.getString(
					R.string.fragment_series_item_count_content_description,
					collection.unwatchedCount
				)
			}

			binding.root.setOnClickListener {
				listener.onCollectionClicked(collection)
			}
			binding.menu.setOnClickListener {
				listener.onCollectionMenuClicked(collection, binding.menu)
			}
		}
	}
}
