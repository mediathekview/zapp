package de.christinecoenen.code.zapp.app.livestream.repository

import de.christinecoenen.code.zapp.app.livestream.api.IZappBackendApiService
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel.Companion.getById
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProgramInfoRepository(private val zappApi: IZappBackendApiService) {

	private val cache = ProgramInfoCache()

	/**
	 * @throws IllegalArgumentException if a wrong channel id is passed
	 * @throws RuntimeException if api response is empty and no show in cache
	 */
	suspend fun getShow(channelId: String): LiveShow = withContext(Dispatchers.IO) {
		val newChannel = getById(channelId)
		return@withContext getShow(newChannel)
	}

	private suspend fun getShow(channel: Channel): LiveShow {
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
