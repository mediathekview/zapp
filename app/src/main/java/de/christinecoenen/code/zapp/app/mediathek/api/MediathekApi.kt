package de.christinecoenen.code.zapp.app.mediathek.api

import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.utils.api.UserAgentInterceptor
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body


class MediathekApi {

	private val apiService: MediathekApiService

	init {
		// workaround to avoid SSLHandshakeException on Android 7 devices
		// see: https://stackoverflow.com/questions/39133437/sslhandshakeexception-handshake-failed-on-android-n-7-0
		val spec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
			.tlsVersions(TlsVersion.TLS_1_1, TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
			.build()

		val client: OkHttpClient = OkHttpClient.Builder()
			.connectionSpecs(listOf(spec))
			.addInterceptor(UserAgentInterceptor())
			.build()

		val retrofit = Retrofit.Builder()
			.baseUrl("https://mediathekviewweb.de/api/")
			.client(client)
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.build()

		apiService = retrofit.create(MediathekApiService::class.java)
	}

	fun listShows(@Body queryRequest: QueryRequest): Single<List<MediathekShow>> {
		return apiService
			.listShows(queryRequest)
			.subscribeOn(Schedulers.io())
			.map {
				if (it.result == null || it.err != null) {
					throw RuntimeException("Empty result")
				}
				it.result.results
			}
	}
}
