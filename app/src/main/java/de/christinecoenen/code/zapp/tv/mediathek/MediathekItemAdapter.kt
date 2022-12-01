package de.christinecoenen.code.zapp.tv.mediathek

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowListItemListener
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.UiModel
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.UiModelComparator
import de.christinecoenen.code.zapp.databinding.TvFragmentMediathekListItemBinding
import kotlinx.coroutines.launch

class MediathekItemAdapter(
	private val scope: LifecycleCoroutineScope,
	private val listener: MediathekShowListItemListener?
) :
	PagingDataAdapter<UiModel, MediathekItemViewHolder>(UiModelComparator) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediathekItemViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val binding = TvFragmentMediathekListItemBinding.inflate(inflater, parent, false)
		return MediathekItemViewHolder(binding)
	}

	override fun onBindViewHolder(holder: MediathekItemViewHolder, position: Int) {
		val model = getItem(position) ?: throw RuntimeException("null show not supported")

		val show = if (model is UiModel.MediathekShowModel) {
			model.show
		} else {
			throw RuntimeException("only shows are supported")
		}

		scope.launch {

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
