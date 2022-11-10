package de.christinecoenen.code.zapp.app.personal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.app.downloads.ui.list.adapter.DownloadViewHolder
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentListItemBinding
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import kotlinx.coroutines.launch

// TODO: use another, more spacialized viewholder
class DownloadListAdapter(
	private val scope: LifecycleCoroutineScope,
	private val listener: Listener? = null
) : RecyclerView.Adapter<DownloadViewHolder>() {

	private var persistedShows = mutableListOf<PersistedMediathekShow>()

	fun setShows(shows: List<PersistedMediathekShow>) {
		persistedShows = shows.toMutableList()
		// TODO: use diff util
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = DownloadsFragmentListItemBinding.inflate(layoutInflater, parent, false)
		val holder = DownloadViewHolder(binding)

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

	override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
		scope.launch {
			holder.bindItem(persistedShows[holder.bindingAdapterPosition])
		}
	}

	override fun getItemCount() = persistedShows.size

	interface Listener {
		fun onShowClicked(show: PersistedMediathekShow)
		fun onShowLongClicked(show: PersistedMediathekShow, view: View)
	}
}
