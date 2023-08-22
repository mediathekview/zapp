package de.christinecoenen.code.zapp.persistence

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query

@Dao
interface SearchDao {

	@Query("SELECT topic FROM PersistedMediathekShow WHERE topic LIKE :searchQuery UNION SELECT title FROM PersistedMediathekShow WHERE title LIKE :searchQuery")
	fun getLocalSearchSuggestions(searchQuery: String): PagingSource<Int, String>

}
