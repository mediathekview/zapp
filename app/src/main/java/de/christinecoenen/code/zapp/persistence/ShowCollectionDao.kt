package de.christinecoenen.code.zapp.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.christinecoenen.code.zapp.models.collections.ShowCollection
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime

@Dao
interface ShowCollectionDao {

	@Query("SELECT * FROM ShowCollection ORDER BY name COLLATE NOCASE ASC")
	fun getAll(): Flow<List<ShowCollection>>

	@Query("SELECT * FROM ShowCollection ORDER BY createdAt DESC LIMIT :limit")
	fun getRecent(limit: Int): Flow<List<ShowCollection>>

	@Query("SELECT * FROM ShowCollection")
	suspend fun getAllSync(): List<ShowCollection>

	@Query("UPDATE ShowCollection SET lastKnownShowTimestamp=:timestamp WHERE id=:id")
	suspend fun updateLastKnownShowTimestamp(id: Int, timestamp: Int)

	@Query("SELECT * FROM ShowCollection WHERE id=:id")
	fun getFromId(id: Int): Flow<ShowCollection?>

	@Query("SELECT * FROM ShowCollection WHERE id=:id")
	suspend fun getFromIdSync(id: Int): ShowCollection?

	@Insert
	suspend fun insert(collection: ShowCollection): Long

	@Update
	suspend fun update(collection: ShowCollection)

	@Query("UPDATE ShowCollection SET totalCount=:totalCount, unwatchedCount=:unwatchedCount WHERE id=:id")
	suspend fun updateCounters(id: Int, totalCount: Int, unwatchedCount: Int)

	@Query("UPDATE ShowCollection SET totalCount=:totalCount, unwatchedCount=:unwatchedCount, countUpdatedAt=:updatedAt WHERE id=:id")
	suspend fun updateCountersAndRefreshDate(
		id: Int,
		totalCount: Int,
		unwatchedCount: Int,
		updatedAt: DateTime
	)

	@Delete
	suspend fun delete(collection: ShowCollection)
}
