package de.christinecoenen.code.zapp.app.mediathek.api

import android.content.Context
import de.christinecoenen.code.zapp.R
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MediathekApiServiceFactory(
	private val context: Context,
	private val client: OkHttpClient
) {

	fun create(): IMediathekApiService {
		val retrofit = Retrofit.Builder()
			.baseUrl(context.getString(R.string.mediathek_backend_url))
			.client(client)
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		return retrofit.create(IMediathekApiService::class.java)
	}

}
