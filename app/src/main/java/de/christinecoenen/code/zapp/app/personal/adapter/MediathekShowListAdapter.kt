package de.christinecoenen.code.zapp.app.personal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekItemType
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekItemViewHolder
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentItemBinding
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.utils.view.PersistedMediathekShowDiffUtilCallback
import kotlinx.coroutines.launch

class MediathekShowListAdapter(
	private val scope: LifecycleCoroutineScope,
	private val mediathekItemType: MediathekItemType,
	private val listener: Listener? = null
) : RecyclerView.Adapter<MediathekItemViewHolder>() {

	private var persistedShows = mutableListOf<PersistedMediathekShow>()

	fun setShows(shows: List<PersistedMediathekShow>) {
		val diffCallback = PersistedMediathekShowDiffUtilCallback(persistedShows, shows)
		val diffResult = DiffUtil.calculateDiff(diffCallback)

		this.persistedShows.clear()
		this.persistedShows.addAll(shows)

		diffResult.dispatchUpdatesTo(this)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediathekItemViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = MediathekListFragmentItemBinding.inflate(layoutInflater, parent, false)
		val holder = MediathekItemViewHolder(binding, mediathekItemType, true)

		binding.root.setOnClickListener {
			listener?.onShowClicked(persistedShows[holder.bindingAdapterPosition])
		}

		binding.root.setOnLongClickListener {
			listener?.onShowLongClicked(
				persistedShows[holder.bindingAdapterPosition],
				binding.root
			)
			true
		}

		return holder
	}

	override fun onBindViewHolder(holder: MediathekItemViewHolder, position: Int) {
		scope.launch {
			holder.setShow(persistedShows[holder.bindingAdapterPosition].mediathekShow)
		}
	}

	override fun onViewRecycled(holder: MediathekItemViewHolder) {
		super.onViewRecycled(holder)
		holder.recycle()
	}

	override fun getItemCount() = persistedShows.size

	interface Listener {
		fun onShowClicked(show: PersistedMediathekShow)
		fun onShowLongClicked(show: PersistedMediathekShow, view: View)
	}
}
