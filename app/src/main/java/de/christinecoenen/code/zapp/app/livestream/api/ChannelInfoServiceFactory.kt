package de.christinecoenen.code.zapp.app.livestream.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChannelInfoServiceFactory {

	companion object {

		fun get(client: OkHttpClient): IChannelInfoService {
			return Retrofit.Builder()
				.baseUrl("https://api.zapp.mediathekview.de/v1/")
				.client(client)
				.addConverterFactory(GsonConverterFactory.create())
				.build()
				.create(IChannelInfoService::class.java)
		}

	}
}
