package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import de.christinecoenen.code.zapp.databinding.FragmentMediathekListItemBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MediathekItemAdapter(
	diffCallback: DiffUtil.ItemCallback<MediathekShow>,
	private val listener: ListItemListener?
) :
	PagingDataAdapter<MediathekShow, MediathekItemViewHolder>(diffCallback) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediathekItemViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val binding = FragmentMediathekListItemBinding.inflate(inflater, parent, false)
		return MediathekItemViewHolder(binding)
	}

	override fun onBindViewHolder(holder: MediathekItemViewHolder, position: Int) {
		val show = getItem(position) ?: throw RuntimeException("null show not supported")

		GlobalScope.launch {

			holder.itemView.setOnClickListener { listener?.onShowClicked(show) }
			holder.itemView.setOnLongClickListener { view ->
				if (listener != null) {
					listener.onShowLongClicked(show, view)
					true
				} else {
					false
				}
			}

			holder.setShow(show)
		}
	}
}
