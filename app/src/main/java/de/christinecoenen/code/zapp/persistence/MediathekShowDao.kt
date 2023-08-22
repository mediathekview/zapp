package de.christinecoenen.code.zapp.persistence

import androidx.paging.PagingSource
import androidx.room.*
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.models.shows.SortableMediathekShow
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime

@Dao
interface MediathekShowDao {

	@RewriteQueriesToDropUnusedColumns
	@Query(
		"SELECT * FROM (" +
			"SELECT *, downloadedAt as sortDate FROM PersistedMediathekShow WHERE (downloadStatus IN (1,2,3,4,6,9)) UNION " +
			"SELECT *, lastPlayedBackAt as sortDate FROM PersistedMediathekShow WHERE playbackPosition UNION " +
			"SELECT *, bookmarkedAt as sortDate FROM PersistedMediathekShow WHERE bookmarked" +
			") " +
			"WHERE topic LIKE :searchQuery OR title LIKE :searchQuery " +
			"ORDER BY sortDate DESC"
	)
	fun getPersonalShows(searchQuery: String): PagingSource<Int, SortableMediathekShow>

	@Query("SELECT * FROM PersistedMediathekShow")
	fun getAll(): Flow<List<PersistedMediathekShow>>

	@RewriteQueriesToDropUnusedColumns
	@Query("SELECT *, downloadedAt as sortDate FROM PersistedMediathekShow WHERE (downloadStatus IN (1,2,3,4,6,9)) AND (topic LIKE :searchQuery OR title LIKE :searchQuery) ORDER BY downloadedAt DESC")
	fun getAllDownloads(searchQuery: String): PagingSource<Int, SortableMediathekShow>

	@RewriteQueriesToDropUnusedColumns
	@Query("SELECT * FROM PersistedMediathekShow WHERE (downloadStatus IN (1,2,3,4,6,9)) ORDER BY downloadedAt DESC LIMIT :limit")
	fun getDownloads(limit: Int): Flow<List<MediathekShow>>

	@RewriteQueriesToDropUnusedColumns
	@Query("SELECT *, lastPlayedBackAt as sortDate FROM PersistedMediathekShow WHERE playbackPosition AND (topic LIKE :searchQuery OR title LIKE :searchQuery) ORDER BY lastPlayedBackAt DESC")
	fun getAllStarted(searchQuery: String): PagingSource<Int, SortableMediathekShow>

	@RewriteQueriesToDropUnusedColumns
	@Query("SELECT * FROM PersistedMediathekShow WHERE playbackPosition ORDER BY lastPlayedBackAt DESC LIMIT :limit")
	fun getStarted(limit: Int): Flow<List<MediathekShow>>

	@RewriteQueriesToDropUnusedColumns
	@Query("SELECT *, bookmarkedAt as sortDate FROM PersistedMediathekShow WHERE bookmarked AND (topic LIKE :searchQuery OR title LIKE :searchQuery) ORDER BY bookmarkedAt DESC")
	fun getAllBookarked(searchQuery: String): PagingSource<Int, SortableMediathekShow>

	@RewriteQueriesToDropUnusedColumns
	@Query("SELECT * FROM PersistedMediathekShow WHERE bookmarked ORDER BY bookmarkedAt DESC LIMIT :limit")
	fun getBookmarked(limit: Int): Flow<List<MediathekShow>>

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

	@Query("SELECT downloadStatus FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getDownloadStatus(apiId: String): Flow<DownloadStatus>

	@Query("SELECT downloadProgress FROM PersistedMediathekShow WHERE id=:id")
	fun getDownloadProgress(id: Int): Flow<Int>

	@Query("SELECT downloadProgress FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getDownloadProgress(apiId: String): Flow<Int?>

	@Query("SELECT bookmarked FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getIsBookmarked(apiId: String): Flow<Boolean>

	@Query("SELECT 1 FROM PersistedMediathekShow WHERE apiId=:apiId AND (bookmarked == 1 OR playbackPosition > 0 OR downloadStatus IN (1,2,3,4,6,9))")
	fun getIsRelevantForUser(apiId: String): Flow<Boolean?>

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

	@Query("UPDATE PersistedMediathekShow SET bookmarked=:isBookmarked, bookmarkedAt=:bookmarkedAt WHERE apiId=:apiId")
	suspend fun updateIsBookmarked(apiId: String, isBookmarked: Boolean, bookmarkedAt: DateTime?)

	@Query("UPDATE PersistedMediathekShow SET playbackPosition=:positionMillis, videoDuration=:durationMillis, lastPlayedBackAt=:lastPlayedBackAt WHERE id=:id")
	suspend fun setPlaybackPosition(
		id: Int,
		positionMillis: Long,
		durationMillis: Long,
		lastPlayedBackAt: DateTime
	)

	@Query("UPDATE PersistedMediathekShow SET playbackPosition=0 WHERE apiId=:apiId")
	suspend fun resetPlaybackPosition(apiId: String)

	@Query("SELECT playbackPosition FROM PersistedMediathekShow WHERE id=:id")
	suspend fun getPlaybackPosition(id: Int): Long

	@Query("SELECT (CAST(playbackPosition AS FLOAT) / videoDuration) FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getPlaybackPositionPercent(apiId: String): Flow<Float>

	@Query("SELECT downloadedVideoPath FROM PersistedMediathekShow WHERE apiId=:apiId AND downloadStatus=4")
	fun getCompletetlyDownloadedVideoPath(apiId: String): Flow<String?>

	@Delete
	suspend fun delete(show: PersistedMediathekShow)

	@Query("SELECT topic FROM PersistedMediathekShow WHERE topic LIKE :searchQuery UNION SELECT title FROM PersistedMediathekShow WHERE title LIKE :searchQuery")
	fun getLocalSearchSuggestions(searchQuery: String): PagingSource<Int, String>
}
