package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.FragmentMediathekListItemBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow

class MediathekItemAdapter(private val listener: ListItemListener?) : RecyclerView.Adapter<BaseViewHolder>() {

	companion object {
		private const val VIEW_TYPE_ITEM = 0
		private const val VIEW_TYPE_FOOTER = 1
	}

	private var shows: MutableList<MediathekShow> = mutableListOf()
	private var progressBar: ProgressBar? = null

	init {
		setHasStableIds(true)
	}

	fun setShows(shows: List<MediathekShow>) {
		if (shows == this.shows) {
			return
		}

		this.shows.clear()
		this.shows.addAll(shows)

		notifyDataSetChanged()
	}

	fun addShows(shows: List<MediathekShow>) {
		val hasChanged = this.shows.addAll(shows)

		if (hasChanged) {
			notifyDataSetChanged()
		}
	}

	fun setLoading(loading: Boolean) {
		progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
	}

	override fun getItemCount() = shows.size + 1

	override fun getItemId(position: Int): Long {
		// fixed id for footer
		return if (position == shows.size) 0 else shows[position].hashCode().toLong()
	}

	override fun getItemViewType(position: Int): Int {
		return if (position == shows.size) VIEW_TYPE_FOOTER else VIEW_TYPE_ITEM
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
		val inflater = LayoutInflater.from(parent.context)

		return when (viewType) {
			VIEW_TYPE_ITEM -> {
				val binding = FragmentMediathekListItemBinding.inflate(inflater, parent, false)
				MediathekItemViewHolder(binding)
			}
			VIEW_TYPE_FOOTER -> {
				val view = inflater.inflate(R.layout.fragment_mediathek_list_item_footer, parent, false)
				progressBar = view.findViewById(R.id.progress)
				BaseViewHolder(view)
			}
			else -> throw IllegalArgumentException("unknown view type $viewType.")
		}
	}

	override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
		if (position == shows.size) {
			// nothing to do for footer
			return
		}

		val show = shows[position]
		val itemHodler = holder as MediathekItemViewHolder

		itemHodler.setShow(show)

		itemHodler.itemView.setOnClickListener { listener?.onShowClicked(show) }
		itemHodler.itemView.setOnLongClickListener { view ->
			if (listener != null) {
				listener.onShowLongClicked(show, view)
				true
			} else {
				false
			}
		}
	}
}
