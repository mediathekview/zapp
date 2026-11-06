package de.christinecoenen.code.zapp.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.christinecoenen.code.zapp.models.collections.ShowCollectionEntry

@Dao
interface ShowCollectionEntryDao {

	@Query("DELETE FROM ShowCollectionEntry WHERE collectionId=:collectionId")
	suspend fun deleteForCollection(collectionId: Int)

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insert(entries: List<ShowCollectionEntry>)

	/**
	 * Replaces all entries of the given collection with the given shows.
	 */
	@Transaction
	suspend fun replaceForCollection(collectionId: Int, apiIds: Collection<String>) {
		deleteForCollection(collectionId)
		insert(apiIds.map { apiId -> ShowCollectionEntry(collectionId, apiId) })
	}

	/**
	 * Number of shows that belong to the collection.
	 */
	@Query("SELECT COUNT(*) FROM ShowCollectionEntry WHERE collectionId=:collectionId")
	suspend fun countForCollection(collectionId: Int): Int

	/**
	 * Number of shows that belong to the collection and have not been watched yet.
	 */
	@Query(
		"SELECT COUNT(*) FROM ShowCollectionEntry e " +
			"LEFT JOIN PersistedMediathekShow s ON s.apiId = e.apiId " +
			"WHERE e.collectionId=:collectionId " +
			"AND (s.apiId IS NULL OR s.videoDuration <= 0 OR s.playbackPosition < s.videoDuration)"
	)
	suspend fun countUnwatchedForCollection(collectionId: Int): Int
}
