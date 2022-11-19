package de.christinecoenen.code.zapp.app.personal.details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekItemType
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekItemViewHolder
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowListItemListener
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentItemBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.utils.view.MediathekShowDiffUtilItemCallback
import kotlinx.coroutines.launch


class PagedPersistedShowListAdapter(
	private val scope: LifecycleCoroutineScope,
	private val listener: MediathekShowListItemListener
) : PagingDataAdapter<MediathekShow, MediathekItemViewHolder>(
	MediathekShowDiffUtilItemCallback()
) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediathekItemViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = MediathekListFragmentItemBinding.inflate(layoutInflater, parent, false)
		val holder = MediathekItemViewHolder(binding, MediathekItemType.Download, false)

		binding.root.setOnClickListener {
			getItem(holder.bindingAdapterPosition)?.let {
				listener.onShowClicked(it)
			}
		}
		binding.root.setOnLongClickListener {
			getItem(holder.bindingAdapterPosition)?.let {
				listener.onShowLongClicked(it, binding.root)
			}
			true
		}

		return holder
	}

	override fun onBindViewHolder(holder: MediathekItemViewHolder, position: Int) {
		getItem(position)?.let {
			scope.launch {
				holder.setShow(it)
			}
		}
	}

	override fun onViewRecycled(holder: MediathekItemViewHolder) {
		super.onViewRecycled(holder)
		holder.recycle()
	}
}
