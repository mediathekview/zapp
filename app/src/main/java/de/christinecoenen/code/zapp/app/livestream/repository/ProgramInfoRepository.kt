package de.christinecoenen.code.zapp.app.livestream.repository

import de.christinecoenen.code.zapp.app.livestream.api.IZappBackendApiService
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel.Companion.getById
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProgramInfoRepository(private val zappApi: IZappBackendApiService) {

	private val cache: Cache = Cache()

	suspend fun getShows(channelId: String): LiveShow = withContext(Dispatchers.IO) {
		return@withContext try {
			val newChannel = getById(channelId)
			getShows(newChannel)

		} catch (e: IllegalArgumentException) {
			throw Exception("%s is no valid channel id", e)
		}
	}

	private suspend fun getShows(channel: Channel): LiveShow {
		val cachedShow = cache.getShow(channel)

		if (cachedShow != null) {
			return cachedShow
		}

		val showResponse = zappApi.getShows(channel.toString())

		if (!showResponse.isSuccess) {
			throw RuntimeException("Show response was empty")
		}

		val liveShow = showResponse.show.toLiveShow()
		cache.save(channel, liveShow)

		return liveShow
	}

}
