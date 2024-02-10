package de.christinecoenen.code.zapp.repositories

import androidx.paging.PagingSource
import de.christinecoenen.code.zapp.models.search.SearchQuery
import de.christinecoenen.code.zapp.persistence.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime

class SearchRepository(private val database: Database) {

	fun getLocalSearchSuggestions(searchQuery: String): PagingSource<Int, String> {
		return database
			.searchDao()
			.getLocalSearchSuggestions("%$searchQuery%")
	}

	fun getLastQueries(searchQuery: String, limit: Int): PagingSource<Int, String> {
		return database
			.searchDao()
			.getQueries("%$searchQuery%", limit)
	}

	suspend fun saveQuery(query: String) = withContext(Dispatchers.IO) {
		if (query.trim() == "") {
			return@withContext
		}

		database
			.searchDao()
			.saveQuery(SearchQuery(query, DateTime.now()))
	}

	suspend fun deleteQuery(query: String) = withContext(Dispatchers.IO) {
		database
			.searchDao()
			.deleteQuery(query)
	}
}
