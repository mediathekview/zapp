package de.christinecoenen.code.zapp.tv.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.BaseChannelListAdapter
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.IChannelViewBindingAdapter
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.ListItemListener
import de.christinecoenen.code.zapp.databinding.TvFragmentChannelListItemBinding
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
		val binding = TvFragmentChannelListItemBinding.inflate(layoutInflater, parent, false)
		return ChannelViewBindingAdapter(binding)
	}

}
