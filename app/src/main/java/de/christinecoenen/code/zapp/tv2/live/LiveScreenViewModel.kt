package de.christinecoenen.code.zapp.tv2.live

import androidx.lifecycle.ViewModel
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.channels.json.JsonChannelList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LiveScreenViewModel(
	private val channelList: JsonChannelList
) : ViewModel() {

	private val _selectedChannel = MutableStateFlow(channelList.first())
	val selectedChannel = _selectedChannel.asStateFlow()

	val channels
		get() = channelList.list

	fun setSelectedChannel(channel: ChannelModel) {
		_selectedChannel.value = channel
	}
}
