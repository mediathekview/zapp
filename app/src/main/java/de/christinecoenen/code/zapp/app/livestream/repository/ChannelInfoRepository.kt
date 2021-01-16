package de.christinecoenen.code.zapp.app.livestream.repository

import de.christinecoenen.code.zapp.app.livestream.api.ChannelInfoService
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel.Companion.getById
import de.christinecoenen.code.zapp.app.livestream.api.model.ChannelInfo
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import de.christinecoenen.code.zapp.utils.api.UserAgentInterceptor
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ChannelInfoRepository private constructor() {

	companion object {

		private var instance: ChannelInfoRepository? = null

		@JvmStatic
		fun getInstance(): ChannelInfoRepository {
			if (instance == null) {
				instance = ChannelInfoRepository()
			}
			return instance!!
		}
	}


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
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.build()
			.create(ChannelInfoService::class.java)
	}

	fun getShows(channelId: String): Single<LiveShow> {
		return try {
			val newChannel = getById(channelId)
			getShows(newChannel)

		} catch (e: IllegalArgumentException) {
			Single.error(Exception("%s is no valid channel id", e))
		}
	}

	fun getChannelInfoList(): Single<Map<String, ChannelInfo>> {
		return service.getChannelInfoList()
			.subscribeOn(Schedulers.io())
	}

	private fun getShows(channel: Channel): Single<LiveShow> {
		val cachedShow = cache.getShow(channel)

		if (cachedShow != null) {
			return Single.just(cachedShow)
		}

		return service
			.getShows(channel.toString())
			.subscribeOn(Schedulers.io())
			.map { showResponse ->
				if (!showResponse.isSuccess) {
					throw RuntimeException("Show response was empty")
				}

				val liveShow = showResponse.show.toLiveShow()
				cache.save(channel, liveShow)
				liveShow
			}
	}

}
