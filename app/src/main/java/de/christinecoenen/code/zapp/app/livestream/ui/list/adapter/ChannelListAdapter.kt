package de.christinecoenen.code.zapp.app.livestream.ui.list.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.FragmentChannelListItemBinding
import de.christinecoenen.code.zapp.models.channels.IChannelList
import java.util.*


class ChannelListAdapter(
	private val channelList: IChannelList,
	private val listener: ListItemListener
) : RecyclerView.Adapter<ChannelViewHolder>() {

	private val visibleViews: WeakHashMap<ChannelViewHolder, Any> = WeakHashMap()

	init {
		setHasStableIds(true)
	}

	fun pause() {
		visibleViews.keys.forEach { it.pause() }
	}

	fun resume() {
		notifyDataSetChanged()

		visibleViews.keys.forEach { it.resume() }
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = FragmentChannelListItemBinding.inflate(layoutInflater, parent, false)
		return ChannelViewHolder(binding, listener)
	}

	override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
		val channel = channelList[position]
		holder.setChannel(channel)
	}

	override fun onViewDetachedFromWindow(holder: ChannelViewHolder) {
		holder.pause()
		visibleViews.remove(holder)
	}

	override fun onViewAttachedToWindow(holder: ChannelViewHolder) {
		holder.resume()
		visibleViews[holder] = null
	}

	override fun getItemCount() = channelList.size()

	override fun getItemId(position: Int) = position.toLong()
}
