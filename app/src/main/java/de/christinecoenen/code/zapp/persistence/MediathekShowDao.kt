package de.christinecoenen.code.zapp.persistence

import androidx.paging.DataSource
import androidx.room.*
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.joda.time.DateTime

@Dao
interface MediathekShowDao {

	@Query("SELECT * FROM PersistedMediathekShow")
	fun getAll(): Flowable<List<PersistedMediathekShow>>

	@Query("SELECT * FROM PersistedMediathekShow WHERE downloadStatus!=0 AND downloadStatus!=7 AND downloadStatus!=8 ORDER BY downloadedAt DESC")
	fun getAllDownloads(): DataSource.Factory<Int, PersistedMediathekShow>

	@Query("SELECT * FROM PersistedMediathekShow WHERE id=:id")
	fun getFromId(id: Int): Flowable<PersistedMediathekShow>

	@Query("SELECT * FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getFromApiId(apiId: String): Flowable<PersistedMediathekShow>

	@Query("SELECT * FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getFromApiIdSync(apiId: String): PersistedMediathekShow?

	@Query("SELECT * FROM PersistedMediathekShow WHERE downloadId=:downloadId")
	fun getFromDownloadId(downloadId: Int): Flowable<PersistedMediathekShow>

	@Query("SELECT downloadStatus FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getDownloadStatus(apiId: String): Flowable<DownloadStatus>

	@Query("SELECT downloadProgress FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getDownloadProgress(apiId: String): Flowable<Int>

	@Insert
	fun insert(vararg show: PersistedMediathekShow): Completable

	@Update
	fun update(vararg show: PersistedMediathekShow): Completable

	@Transaction
	fun insertOrUpdate(show: MediathekShow) {
		val existingPersistedShow = getFromApiIdSync(show.apiId)

		if (existingPersistedShow == null) {
			// insert new show
			val newPersistedShow = PersistedMediathekShow(
				mediathekShow = show
			)
			insert(newPersistedShow).blockingAwait()
		} else {
			// update existing show
			existingPersistedShow.mediathekShow = show
			update(existingPersistedShow).blockingAwait()
		}
	}

	@Query("UPDATE PersistedMediathekShow SET downloadStatus=:downloadStatus WHERE downloadId=:downloadId")
	fun updateDownloadStatus(downloadId: Int, downloadStatus: DownloadStatus): Completable

	@Query("UPDATE PersistedMediathekShow SET downloadProgress=:progress WHERE downloadId=:downloadId")
	fun updateDownloadProgress(downloadId: Int, progress: Int): Completable

	@Query("UPDATE PersistedMediathekShow SET downloadedVideoPath=:videoPath WHERE downloadId=:downloadId")
	fun updateDownloadedVideoPath(downloadId: Int, videoPath: String): Completable

	@Query("UPDATE PersistedMediathekShow SET playbackPosition=:positionMillis, videoDuration=:durationMillis, lastPlayedBackAt=:lastPlayedBackAt WHERE id=:id")
	fun setPlaybackPosition(id: Int, positionMillis: Long, durationMillis: Long, lastPlayedBackAt: DateTime): Completable

	@Query("SELECT playbackPosition FROM PersistedMediathekShow WHERE id=:id")
	fun getPlaybackPosition(id: Int): Single<Long>

	@Query("SELECT (CAST(playbackPosition AS FLOAT) / videoDuration) FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getPlaybackPositionPercent(apiId: String): Flowable<Float>

	@Delete
	fun delete(show: PersistedMediathekShow): Completable
}
