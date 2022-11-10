package de.christinecoenen.code.zapp.app.personal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentItemBinding
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import kotlinx.coroutines.launch

// TODO: use another, more spacialized viewholder
class DownloadListAdapter(
	private val scope: LifecycleCoroutineScope,
	private val listener: Listener? = null
) : RecyclerView.Adapter<DownloadItemViewHolder>() {

	private var persistedShows = mutableListOf<PersistedMediathekShow>()

	public fun setShows(shows: List<PersistedMediathekShow>) {
		persistedShows = shows.toMutableList()
		// TODO: use diff util
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = MediathekListFragmentItemBinding.inflate(layoutInflater, parent, false)
		val holder = DownloadItemViewHolder(binding)

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

	override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
		scope.launch {
			holder.setShow(persistedShows[holder.bindingAdapterPosition].mediathekShow)
		}
	}

	override fun getItemCount() = persistedShows.size

	interface Listener {
		fun onShowClicked(show: PersistedMediathekShow)
		fun onShowLongClicked(show: PersistedMediathekShow, view: View)
	}
}
