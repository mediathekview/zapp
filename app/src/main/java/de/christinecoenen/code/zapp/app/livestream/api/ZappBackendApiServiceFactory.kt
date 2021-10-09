package de.christinecoenen.code.zapp.app.livestream.api

import android.content.Context
import de.christinecoenen.code.zapp.R
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ZappBackendApiServiceFactory(
	private val context: Context,
	private val client: OkHttpClient
) {

	fun create(): IZappBackendApiService {
		return Retrofit.Builder()
			.baseUrl(context.getString(R.string.zapp_backend_url))
			.client(client)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
			.create(IZappBackendApiService::class.java)
	}

}
