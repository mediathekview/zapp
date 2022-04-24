package de.christinecoenen.code.zapp.app.livestream.ui.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import de.christinecoenen.code.zapp.databinding.ChannelListFragmentItemBinding
import de.christinecoenen.code.zapp.models.channels.IChannelList


class ChannelListAdapter(
	channelList: IChannelList,
	lifecycleOwner: LifecycleOwner,
	listener: ListItemListener
) : BaseChannelListAdapter(channelList, lifecycleOwner, listener) {

	init {
		setHasStableIds(true)
	}

	override fun getViewBindingAdapter(
		layoutInflater: LayoutInflater,
		parent: ViewGroup
	): IChannelViewBindingAdapter {
		val binding = ChannelListFragmentItemBinding.inflate(layoutInflater, parent, false)
		return ChannelViewBindingAdapter(binding)
	}

}
