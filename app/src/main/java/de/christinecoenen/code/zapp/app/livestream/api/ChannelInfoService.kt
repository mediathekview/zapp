package de.christinecoenen.code.zapp.app.livestream.api

import de.christinecoenen.code.zapp.app.livestream.api.model.ChannelInfo
import de.christinecoenen.code.zapp.app.livestream.api.model.ShowResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface ChannelInfoService {

	@GET("shows/{channelName}")
	fun getShows(@Path("channelName") channelName: String): Single<ShowResponse>

	@GET("channelInfoList")
	fun getChannelInfoList(): Single<Map<String, ChannelInfo>>
}
