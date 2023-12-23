package de.christinecoenen.code.zapp.app.search.suggestions.text

import android.view.View

interface SuggestionTextListener {
	fun onSuggestionSelected(suggestion: String)
	fun onSuggestionInserted(suggestion: String)
	fun onSuggestionLongPress(suggestion: String, type: TextSuggestionType, view: View): Boolean
}
