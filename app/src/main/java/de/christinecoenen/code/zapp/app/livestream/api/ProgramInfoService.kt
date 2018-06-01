package de.christinecoenen.code.zapp.app.livestream.api


import de.christinecoenen.code.zapp.app.livestream.api.model.ShowResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface ProgramInfoService {

    @GET("shows/{channelName}")
    fun getShows(@Path("channelName") channelName: String): Call<ShowResponse>

}
