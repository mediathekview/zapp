package de.christinecoenen.code.zapp.persistence

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.christinecoenen.code.zapp.models.search.SearchQuery

@Dao
interface SearchDao {

	@Query("SELECT topic FROM PersistedMediathekShow WHERE topic LIKE :searchQuery UNION SELECT title FROM PersistedMediathekShow WHERE title LIKE :searchQuery")
	fun getLocalSearchSuggestions(searchQuery: String): PagingSource<Int, String>

	@Query("SELECT `query` FROM SearchQuery WHERE `query` LIKE :searchQuery ORDER BY date DESC LIMIT :limit")
	fun getQueries(searchQuery: String, limit: Int): PagingSource<Int, String>

	@Query("DELETE FROM SearchQuery WHERE `query` = :query")
	suspend fun deleteQuery(query: String)

	@Upsert
	suspend fun saveQuery(vararg query: SearchQuery)
	
}
