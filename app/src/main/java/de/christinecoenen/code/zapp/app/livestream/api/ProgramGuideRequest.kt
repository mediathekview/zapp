package de.christinecoenen.code.zapp.app.livestream.api


import de.christinecoenen.code.zapp.app.livestream.api.model.Channel
import de.christinecoenen.code.zapp.app.livestream.api.model.ShowResponse
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class ProgramGuideRequest(val channel: Channel,
							   val listener: Listener) : Callback<ShowResponse> {

	private var showCall: Call<ShowResponse>? = null

	fun execute(): ProgramGuideRequest {
		val cachedShow = Cache.getInstance().getShow(channel)

		if (cachedShow == null) {
			showCall = service.getShows(channel.toString())
			showCall?.enqueue(this)
		} else {
			listener.onRequestSuccess(cachedShow)
		}

		return this
	}

	fun cancel() {
		showCall?.cancel()
	}

	override fun onResponse(call: Call<ShowResponse>, response: Response<ShowResponse>) {
		if (response.body()?.isSuccess == true) {
			val liveShow = response.body()!!.show.toLiveShow()
			Cache.getInstance().save(channel, liveShow)
			listener.onRequestSuccess(liveShow)
		} else {
			listener.onRequestError()
		}
	}

	override fun onFailure(call: Call<ShowResponse>, t: Throwable) {
		if (!call.isCanceled) {
			listener.onRequestError()
		}
	}

	interface Listener {
		fun onRequestError()

		fun onRequestSuccess(currentShow: LiveShow)
	}

	companion object {

		private val service = Retrofit.Builder()
			.baseUrl("https://zappbackend.herokuapp.com/v1/")
			.addConverterFactory(GsonConverterFactory.create())
			.build()
			.create(ProgramInfoService::class.java)
	}
}
