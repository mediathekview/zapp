package de.christinecoenen.code.zapp.app.livestream.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.repositories.ChannelRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class ChannelPlayerActivityViewModel(channelRepository: ChannelRepository) : ViewModel() {

	private val channelList = channelRepository.getChannelList()
	private var channelId = MutableSharedFlow<String>(replay = 1)

	private val channelFlow = channelId
		.map { channelId -> channelList[channelId]!! }

	val channel = channelFlow.asLiveData(viewModelScope.coroutineContext)

	suspend fun setChannelId(channelId: String): ChannelModel {
		this.channelId.emit(channelId)
		return channelFlow.first()
	}

}
