package de.christinecoenen.code.zapp.app.downloads.ui.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentListItemBinding
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DownloadListAdapter(
	private val scope: LifecycleCoroutineScope,
	private val listener: Listener
) : PagingDataAdapter<PersistedMediathekShow, DownloadViewHolder>(DownloadDiffUtilCallback()) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = DownloadsFragmentListItemBinding.inflate(layoutInflater, parent, false)
		val holder = DownloadViewHolder(binding)

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

	override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
		getItem(position)?.let {
			scope.launch(Dispatchers.Main) {
				holder.bindItem(it)
			}
		}
	}

	interface Listener {
		fun onShowClicked(show: PersistedMediathekShow)
		fun onShowLongClicked(show: PersistedMediathekShow, view: View)
	}
}
