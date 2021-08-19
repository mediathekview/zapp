package de.christinecoenen.code.zapp.repositories

import androidx.paging.PagingSource
import de.christinecoenen.code.zapp.app.mediathek.api.MediathekApi
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.persistence.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import retrofit2.http.Body

class MediathekRepository(
	private val mediathekApi: MediathekApi,
	private val database: Database
) {

	val downloads: PagingSource<Int, PersistedMediathekShow>
		get() = database.mediathekShowDao().getAllDownloads()

	suspend fun listShows(@Body queryRequest: QueryRequest): List<MediathekShow> =
		withContext(Dispatchers.IO) {
			mediathekApi
				.listShows(queryRequest)
		}

	suspend fun persistOrUpdateShow(show: MediathekShow): Flow<PersistedMediathekShow> =
		withContext(Dispatchers.IO) {
			database
				.mediathekShowDao()
				.insertOrUpdate(show)

			database
				.mediathekShowDao()
				.getFromApiId(show.apiId)
				.flowOn(Dispatchers.IO)
		}

	suspend fun updateShow(show: PersistedMediathekShow?) = withContext(Dispatchers.IO) {
		database
			.mediathekShowDao()
			.update(show!!)
	}

	suspend fun updateDownloadStatus(downloadId: Int, downloadStatus: DownloadStatus?) =
		withContext(Dispatchers.IO) {
			database
				.mediathekShowDao()
				.updateDownloadStatus(downloadId, downloadStatus!!)
		}

	suspend fun updateDownloadProgress(downloadId: Int, progress: Int) =
		withContext(Dispatchers.IO) {
			database
				.mediathekShowDao()
				.updateDownloadProgress(downloadId, progress)
		}

	suspend fun updateDownloadedVideoPath(downloadId: Int, videoPath: String?) =
		withContext(Dispatchers.IO) {
			database
				.mediathekShowDao()
				.updateDownloadedVideoPath(downloadId, videoPath!!)
		}

	fun getPersistedShow(id: Int): Flow<PersistedMediathekShow> {
		return database
			.mediathekShowDao()
			.getFromId(id)
			.flowOn(Dispatchers.IO)
	}

	fun getPersistedShowByApiId(apiId: String): Flow<PersistedMediathekShow> {
		return database
			.mediathekShowDao()
			.getFromApiId(apiId)
			.filterNotNull()
			.flowOn(Dispatchers.IO)
	}

	fun getPersistedShowByDownloadId(downloadId: Int): Flow<PersistedMediathekShow> {
		return database
			.mediathekShowDao()
			.getFromDownloadId(downloadId)
			.flowOn(Dispatchers.IO)
	}

	fun getDownloadStatus(apiId: String): Flow<DownloadStatus> {
		return database
			.mediathekShowDao()
			.getDownloadStatus(apiId)
			.onStart { emit(DownloadStatus.NONE) }
			.flowOn(Dispatchers.IO)
	}

	fun getDownloadProgress(apiId: String): Flow<Int> {
		return database
			.mediathekShowDao()
			.getDownloadProgress(apiId)
			.flowOn(Dispatchers.IO)
	}

	suspend fun getPlaybackPosition(showId: Int): Long = withContext(Dispatchers.IO) {
		database
			.mediathekShowDao()
			.getPlaybackPosition(showId)
	}

	suspend fun setPlaybackPosition(showId: Int, positionMillis: Long, durationMillis: Long) =
		withContext(Dispatchers.IO) {
			database
				.mediathekShowDao()
				.setPlaybackPosition(showId, positionMillis, durationMillis, DateTime.now())
		}

	fun getPlaybackPositionPercent(apiId: String): Flow<Float> {
		return database
			.mediathekShowDao()
			.getPlaybackPositionPercent(apiId)
			.filterNotNull()
			.onStart { emit(0f) }
			.flowOn(Dispatchers.IO)
	}
}
