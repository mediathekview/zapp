package de.christinecoenen.code.zapp.app.livestream.ui.list.adapter

import android.view.View
import android.view.View.OnLongClickListener
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.FragmentChannelListItemBinding
import de.christinecoenen.code.zapp.models.channels.ChannelModel

class ChannelViewHolder(
	private val binding: FragmentChannelListItemBinding,
	private val listener: ListItemListener
) : RecyclerView.ViewHolder(binding.root), View.OnClickListener, OnLongClickListener {

	private var channel: ChannelModel? = null

	init {
		binding.root.setOnLongClickListener(this)
		binding.root.setOnClickListener(this)
	}

	override fun onClick(view: View) {
		channel?.let { listener.onItemClick(it) }
	}

	override fun onLongClick(view: View): Boolean {
		channel?.let { listener.onItemLongClick(it, view) }
		return true
	}

	fun setChannel(channel: ChannelModel) {
		this.channel = channel

		binding.logo.setImageResource(channel.drawableId)
		binding.logo.contentDescription = channel.name

		if (channel.subtitle == null) {
			binding.subtitle.isVisible = false
		} else {
			binding.subtitle.text = channel.subtitle
			binding.subtitle.isVisible = true
		}

		binding.programInfo.setChannel(channel)
	}

	fun pause() {
		binding.programInfo.pause()
	}

	fun resume() {
		binding.programInfo.resume()
	}
}
