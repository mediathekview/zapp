package de.christinecoenen.code.zapp.repositories

import androidx.paging.PagingSource
import de.christinecoenen.code.zapp.persistence.Database

class SearchRepository(private val database: Database) {

	fun getLocalSearchSuggestions(searchQuery: String): PagingSource<Int, String> {
		return database
			.searchDao()
			.getLocalSearchSuggestions("%$searchQuery%")
	}

}
