package de.christinecoenen.code.zapp.app.livestream.repository

import de.christinecoenen.code.zapp.app.livestream.api.ChannelInfoService
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel.Companion.getById
import de.christinecoenen.code.zapp.app.livestream.api.model.ChannelInfo
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import de.christinecoenen.code.zapp.utils.api.UserAgentInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChannelInfoRepository {

	
	private val cache: Cache = Cache()
	private val service: ChannelInfoService


	init {
		val client: OkHttpClient = OkHttpClient.Builder()
			.addInterceptor(UserAgentInterceptor())
			.build()

		service = Retrofit.Builder()
			.baseUrl("https://api.zapp.mediathekview.de/v1/")
			.client(client)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
			.create(ChannelInfoService::class.java)
	}

	suspend fun getShows(channelId: String): LiveShow = withContext(Dispatchers.IO) {
		return@withContext try {
			val newChannel = getById(channelId)
			getShows(newChannel)

		} catch (e: IllegalArgumentException) {
			throw Exception("%s is no valid channel id", e)
		}
	}

	suspend fun getChannelInfoList(): Map<String, ChannelInfo> = withContext(Dispatchers.IO) {
		return@withContext service.getChannelInfoList()
	}

	private suspend fun getShows(channel: Channel): LiveShow {
		val cachedShow = cache.getShow(channel)

		if (cachedShow != null) {
			return cachedShow
		}

		val showResponse = service.getShows(channel.toString())

		if (!showResponse.isSuccess) {
			throw RuntimeException("Show response was empty")
		}

		val liveShow = showResponse.show.toLiveShow()
		cache.save(channel, liveShow)

		return liveShow
	}

}
