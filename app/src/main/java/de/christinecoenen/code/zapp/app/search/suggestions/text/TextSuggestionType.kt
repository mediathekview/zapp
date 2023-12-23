package de.christinecoenen.code.zapp.app.search.suggestions.text

import androidx.annotation.DrawableRes
import de.christinecoenen.code.zapp.R

enum class TextSuggestionType(@DrawableRes val iconResId: Int) {
	RecentQuery(R.drawable.ic_history_24),
	Visited(R.drawable.ic_baseline_search_24),
}
