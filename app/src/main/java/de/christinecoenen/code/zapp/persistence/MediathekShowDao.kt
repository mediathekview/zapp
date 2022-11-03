package de.christinecoenen.code.zapp.persistence

import androidx.paging.PagingSource
import androidx.room.*
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime

@Dao
interface MediathekShowDao {

	@Query("SELECT * FROM PersistedMediathekShow")
	fun getAll(): Flow<List<PersistedMediathekShow>>

	@Query("SELECT * FROM PersistedMediathekShow WHERE downloadStatus IN (1,2,3,4,6,9) ORDER BY downloadedAt DESC")
	fun getAllDownloads(): PagingSource<Int, PersistedMediathekShow>

	@Query("SELECT * FROM PersistedMediathekShow WHERE id=:id")
	fun getFromId(id: Int): Flow<PersistedMediathekShow>

	@Query("SELECT * FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getFromApiId(apiId: String): Flow<PersistedMediathekShow>

	@Query("SELECT * FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getFromApiIdSync(apiId: String): PersistedMediathekShow?

	@Query("SELECT * FROM PersistedMediathekShow WHERE downloadId=:downloadId")
	fun getFromDownloadId(downloadId: Int): Flow<PersistedMediathekShow>

	@Query("SELECT downloadStatus FROM PersistedMediathekShow WHERE id=:id")
	fun getDownloadStatus(id: Int): Flow<DownloadStatus>

	@Query("SELECT downloadProgress FROM PersistedMediathekShow WHERE id=:id")
	fun getDownloadProgress(id: Int): Flow<Int>

	@Query("SELECT * FROM PersistedMediathekShow WHERE downloadStatus=4")
	fun getCompletedDownloads(): Flow<List<PersistedMediathekShow>>

	@Insert
	suspend fun insert(vararg show: PersistedMediathekShow)

	@Update
	suspend fun update(vararg show: PersistedMediathekShow)

	@Transaction
	suspend fun insertOrUpdate(show: MediathekShow) {
		val existingPersistedShow = getFromApiIdSync(show.apiId)

		if (existingPersistedShow == null) {
			// insert new show
			val newPersistedShow = PersistedMediathekShow(
				mediathekShow = show
			)
			insert(newPersistedShow)
		} else {
			// update existing show
			existingPersistedShow.mediathekShow = show
			update(existingPersistedShow)
		}
	}

	@Query("UPDATE PersistedMediathekShow SET downloadStatus=:downloadStatus WHERE downloadId=:downloadId")
	suspend fun updateDownloadStatus(downloadId: Int, downloadStatus: DownloadStatus)

	@Query("UPDATE PersistedMediathekShow SET downloadProgress=:progress WHERE downloadId=:downloadId")
	suspend fun updateDownloadProgress(downloadId: Int, progress: Int)

	@Query("UPDATE PersistedMediathekShow SET downloadedVideoPath=:videoPath WHERE downloadId=:downloadId")
	suspend fun updateDownloadedVideoPath(downloadId: Int, videoPath: String?)

	@Query("UPDATE PersistedMediathekShow SET playbackPosition=:positionMillis, videoDuration=:durationMillis, lastPlayedBackAt=:lastPlayedBackAt WHERE id=:id")
	suspend fun setPlaybackPosition(
		id: Int,
		positionMillis: Long,
		durationMillis: Long,
		lastPlayedBackAt: DateTime
	)

	@Query("SELECT playbackPosition FROM PersistedMediathekShow WHERE id=:id")
	suspend fun getPlaybackPosition(id: Int): Long

	@Query("SELECT (CAST(playbackPosition AS FLOAT) / videoDuration) FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getPlaybackPositionPercent(apiId: String): Flow<Float>

	@Delete
	suspend fun delete(show: PersistedMediathekShow)
}
