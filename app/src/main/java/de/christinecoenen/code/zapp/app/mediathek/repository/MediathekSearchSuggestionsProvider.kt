package de.christinecoenen.code.zapp.app.mediathek.repository

import android.content.Context
import android.content.SearchRecentSuggestionsProvider
import android.provider.SearchRecentSuggestions

class MediathekSearchSuggestionsProvider : SearchRecentSuggestionsProvider() {

	companion object {

		private const val AUTHORITY = "de.christinecoenen.code.zapp.MediathekSearchSuggestionsProvider"
		private const val MODE = DATABASE_MODE_QUERIES

		fun saveQuery(context: Context, query: String) {
			val suggestions = SearchRecentSuggestions(context, AUTHORITY, MODE)
			suggestions.saveRecentQuery(query, null)
		}

		@JvmStatic
		fun deleteAllQueries(context: Context) {
			val suggestions = SearchRecentSuggestions(context, AUTHORITY, MODE)
			suggestions.clearHistory()
		}
	}

	init {
		setupSuggestions(AUTHORITY, MODE)
	}
}
