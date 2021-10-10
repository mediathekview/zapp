package de.christinecoenen.code.zapp.app.livestream.api

import de.christinecoenen.code.zapp.app.livestream.api.model.ChannelInfo
import de.christinecoenen.code.zapp.app.livestream.api.model.ShowResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface IZappBackendApiService {

	@GET("shows/{channelName}")
	suspend fun getShows(@Path("channelName") channelName: String): ShowResponse

	@GET("channelInfoList")
	suspend fun getChannelInfoList(): Map<String, ChannelInfo>
}
