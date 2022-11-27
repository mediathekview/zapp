package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentItemBinding
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentItemDateSeparatorBinding


class PagedMediathekShowListAdapter(
	private val scope: LifecycleCoroutineScope,
	private val highlightRelevantForUser: Boolean,
	private val listener: MediathekShowListItemListener
) : PagingDataAdapter<UiModel, RecyclerView.ViewHolder>(UiModelComparator) {

	enum class ViewType {
		Item,
		Header
	}

	override fun getItemViewType(position: Int): Int {
		// Use peek over getItem to avoid triggering page fetch / drops, since
		// recycling views is not indicative of the user's current scroll position.
		return when (peek(position)) {
			is UiModel.MediathekShowModel -> ViewType.Item
			is UiModel.DateSeparatorModel -> ViewType.Header
			null -> throw IllegalStateException("Unknown view")
		}.ordinal
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
		ViewType.Item.ordinal -> createItemViewHolder(parent)
		else -> createHeaderViewHolder(parent)
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is MediathekItemViewHolder -> bindItemViewHolder(holder, position)
			is DateSeparatorViewHolder -> bindHeaderViewHolder(holder, position)
		}
	}

	override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
		super.onViewRecycled(holder)

		if (holder is MediathekItemViewHolder) {
			holder.recycle()
		}
	}

	private fun getShowItem(position: Int): UiModel.MediathekShowModel =
		(getItem(position) as UiModel.MediathekShowModel)

	private fun getDateSeparatorItem(position: Int): UiModel.DateSeparatorModel =
		(getItem(position) as UiModel.DateSeparatorModel)

	private fun bindItemViewHolder(holder: MediathekItemViewHolder, position: Int) {
		holder.setShow(getShowItem(position).show)
	}

	private fun bindHeaderViewHolder(holder: DateSeparatorViewHolder, position: Int) {
		val dateSeparator = getDateSeparatorItem(position)
		holder.bind(dateSeparator.date)
	}

	private fun createItemViewHolder(parent: ViewGroup): MediathekItemViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = MediathekListFragmentItemBinding.inflate(layoutInflater, parent, false)
		val holder = MediathekItemViewHolder(binding, highlightRelevantForUser, scope)

		binding.root.setOnClickListener {
			getShowItem(holder.bindingAdapterPosition).let {
				listener.onShowClicked(it.show)
			}
		}
		binding.root.setOnLongClickListener {
			getShowItem(holder.bindingAdapterPosition).let {
				listener.onShowLongClicked(it.show, binding.root)
			}
			true
		}

		return holder
	}

	private fun createHeaderViewHolder(parent: ViewGroup): DateSeparatorViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding =
			MediathekListFragmentItemDateSeparatorBinding.inflate(layoutInflater, parent, false)
		return DateSeparatorViewHolder(binding)
	}
}
