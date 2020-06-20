package de.christinecoenen.code.zapp.app.mediathek.repository.persistence

import androidx.room.*
import de.christinecoenen.code.zapp.app.mediathek.model.DownloadStatus
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface MediathekShowDao {

	@Query("SELECT * FROM PersistedMediathekShow")
	fun getAll(): Flowable<List<PersistedMediathekShow>>

	@Query("SELECT * FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getFromApiId(apiId: String): Flowable<PersistedMediathekShow>

	@Query("SELECT downloadStatus FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getDownloadStatus(apiId: String): Flowable<DownloadStatus>

	@Query("SELECT downloadProgress FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getDownloadProgress(apiId: String): Flowable<Int>

	@Insert
	fun insert(vararg show: PersistedMediathekShow): Completable

	@Update
	fun update(vararg show: PersistedMediathekShow): Completable

	@Query("UPDATE PersistedMediathekShow SET downloadStatus=:downloadStatus WHERE downloadId=:downloadId")
	fun updateDownloadStatus(downloadId: Long, downloadStatus: DownloadStatus): Completable

	@Query("UPDATE PersistedMediathekShow SET downloadProgress=:progress WHERE downloadId=:downloadId")
	fun updateDownloadProgress(downloadId: Long, progress: Int): Completable

	@Query("UPDATE PersistedMediathekShow SET downloadedVideoPath=:videoPath WHERE downloadId=:downloadId")
	fun updateDownloadedVideoPath(downloadId: Long, videoPath: String): Completable

	@Delete
	fun delete(show: PersistedMediathekShow): Completable
}
