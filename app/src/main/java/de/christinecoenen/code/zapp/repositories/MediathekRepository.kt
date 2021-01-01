package de.christinecoenen.code.zapp.repositories

import androidx.paging.DataSource
import de.christinecoenen.code.zapp.app.mediathek.api.MediathekService
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.app.mediathek.api.result.MediathekAnswer
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.persistence.Database
import de.christinecoenen.code.zapp.utils.api.UserAgentInterceptor
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body

class MediathekRepository(private val database: Database) {

	private val service: MediathekService

	fun listShows(@Body queryRequest: QueryRequest?): Single<List<MediathekShow>> {
		return service.listShows(queryRequest)
			.subscribeOn(Schedulers.io())
			.map { mediathekAnswer: MediathekAnswer ->
				if (mediathekAnswer.result == null || mediathekAnswer.err != null) {
					throw RuntimeException("Empty result")
				}
				mediathekAnswer.result.results
			}
	}

	val downloads: DataSource.Factory<Int, PersistedMediathekShow>
		get() = database.mediathekShowDao().getAllDownloads()


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

		service = retrofit.create(MediathekService::class.java)
	}

	fun persistOrUpdateShow(show: MediathekShow): Flowable<PersistedMediathekShow> {
		return Completable.fromAction { database.mediathekShowDao().insertOrUpdate(show) }
			.andThen(database.mediathekShowDao().getFromApiId(show.apiId))
			.subscribeOn(Schedulers.io())
	}

	fun updateShow(show: PersistedMediathekShow?) {
		database.mediathekShowDao()
			.update(show!!)
			.subscribeOn(Schedulers.io())
			.subscribe()
	}

	fun updateDownloadStatus(downloadId: Int, downloadStatus: DownloadStatus?) {
		database.mediathekShowDao()
			.updateDownloadStatus(downloadId, downloadStatus!!)
			.subscribeOn(Schedulers.io())
			.subscribe()
	}

	fun updateDownloadProgress(downloadId: Int, progress: Int) {
		database.mediathekShowDao()
			.updateDownloadProgress(downloadId, progress)
			.subscribeOn(Schedulers.io())
			.subscribe()
	}

	fun updateDownloadedVideoPath(downloadId: Int, videoPath: String?) {
		database.mediathekShowDao()
			.updateDownloadedVideoPath(downloadId, videoPath!!)
			.subscribeOn(Schedulers.io())
			.subscribe()
	}

	fun getPersistedShow(id: Int): Flowable<PersistedMediathekShow> {
		return database.mediathekShowDao()
			.getFromId(id)
			.subscribeOn(Schedulers.io())
	}

	fun getPersistedShowByApiId(apiId: String): Flowable<PersistedMediathekShow> {
		return database.mediathekShowDao()
			.getFromApiId(apiId)
			.subscribeOn(Schedulers.io())
	}

	fun getPersistedShowByDownloadId(downloadId: Int): Flowable<PersistedMediathekShow> {
		return database.mediathekShowDao().getFromDownloadId(downloadId).subscribeOn(Schedulers.io())
	}

	fun getDownloadStatus(apiId: String): Flowable<DownloadStatus> {
		return database
			.mediathekShowDao()
			.getDownloadStatus(apiId)
			.startWith(DownloadStatus.NONE)
			.subscribeOn(Schedulers.io())
	}

	fun getDownloadProgress(apiId: String): Flowable<Int> {
		return database
			.mediathekShowDao()
			.getDownloadProgress(apiId)
			.subscribeOn(Schedulers.io())
	}

	fun getPlaybackPosition(showId: Int): Single<Long> {
		return database.mediathekShowDao()
			.getPlaybackPosition(showId)
			.subscribeOn(Schedulers.io())
	}

	fun setPlaybackPosition(showId: Int, positionMillis: Long, durationMillis: Long) {
		database.mediathekShowDao()
			.setPlaybackPosition(showId, positionMillis, durationMillis, DateTime.now())
			.subscribeOn(Schedulers.io())
			.subscribe()
	}

	fun getPlaybackPositionPercent(apiId: String): Flowable<Float> {
		return database.mediathekShowDao()
			.getPlaybackPositionPercent(apiId)
			.startWith(0f)
			.distinct()
			.subscribeOn(Schedulers.io())
	}
}
