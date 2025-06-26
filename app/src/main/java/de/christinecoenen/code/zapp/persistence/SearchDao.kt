package de.christinecoenen.code.zapp.persistence

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.christinecoenen.code.zapp.models.search.SearchQuery

@Dao
interface SearchDao {

	@Query(
		"SELECT text FROM (" +
			"SELECT text, MAX(sortDate) as maxSortDate FROM (" +
			"SELECT MAX(downloadedAt, lastPlayedBackAt, bookmarkedAt, showUpdatedAt) as sortDate, topic as text FROM PersistedMediathekShow WHERE topic LIKE :searchQuery UNION " +
			"SELECT MAX(downloadedAt, lastPlayedBackAt, bookmarkedAt, showUpdatedAt) as sortDate, title as text FROM PersistedMediathekShow WHERE title LIKE :searchQuery " +
			") GROUP BY text) " +
			"ORDER BY maxSortDate DESC"
	)
	fun getLocalSearchSuggestions(searchQuery: String): PagingSource<Int, String>

	@Query("SELECT `query` FROM SearchQuery WHERE `query` LIKE :searchQuery ORDER BY date DESC LIMIT :limit")
	fun getQueries(searchQuery: String, limit: Int): PagingSource<Int, String>

	@Query("DELETE FROM SearchQuery WHERE `query` = :query")
	suspend fun deleteQuery(query: String)

	@Query("DELETE FROM SearchQuery")
	suspend fun deleteAllQueries()

	@Upsert
	suspend fun saveQuery(vararg query: SearchQuery)

}
