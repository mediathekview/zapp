package de.christinecoenen.code.zapp.app.search.suggestions.chips

interface SuggestionChipListener<T : ChipContent> {
    fun onChipClick(content: T)
}
