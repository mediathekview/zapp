package de.christinecoenen.code.zapp.repositories

import androidx.paging.PagingSource
import de.christinecoenen.code.zapp.app.mediathek.api.MediathekApi
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.persistence.Database
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import retrofit2.http.Body

class MediathekRepository(
	private val mediathekApi: MediathekApi,
	private val database: Database
) {

	val downloads: PagingSource<Int, PersistedMediathekShow>
		get() = database.mediathekShowDao().getAllDownloads()

	suspend fun listShows(@Body queryRequest: QueryRequest) = mediathekApi.listShows(queryRequest)

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
		return database.mediathekShowDao().getFromDownloadId(downloadId)
			.subscribeOn(Schedulers.io())
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
