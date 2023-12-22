package de.christinecoenen.code.zapp.app.search.suggestions.text

interface SuggestionTextListener {
    fun onSuggestionSelected(suggestion: String)
    fun onSuggestionInserted(suggestion: String)
}
