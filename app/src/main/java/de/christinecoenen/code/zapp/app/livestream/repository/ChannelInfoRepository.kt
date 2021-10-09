package de.christinecoenen.code.zapp.app.livestream.repository

import de.christinecoenen.code.zapp.app.livestream.api.IZappBackendApiService
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel.Companion.getById
import de.christinecoenen.code.zapp.app.livestream.api.model.ChannelInfo
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChannelInfoRepository(private val api: IZappBackendApiService) {

	private val cache: Cache = Cache()

	suspend fun getShows(channelId: String): LiveShow = withContext(Dispatchers.IO) {
		return@withContext try {
			val newChannel = getById(channelId)
			getShows(newChannel)

		} catch (e: IllegalArgumentException) {
			throw Exception("%s is no valid channel id", e)
		}
	}

	suspend fun getChannelInfoList(): Map<String, ChannelInfo> = withContext(Dispatchers.IO) {
		return@withContext api.getChannelInfoList()
	}

	private suspend fun getShows(channel: Channel): LiveShow {
		val cachedShow = cache.getShow(channel)

		if (cachedShow != null) {
			return cachedShow
		}

		val showResponse = api.getShows(channel.toString())

		if (!showResponse.isSuccess) {
			throw RuntimeException("Show response was empty")
		}

		val liveShow = showResponse.show.toLiveShow()
		cache.save(channel, liveShow)

		return liveShow
	}

}
