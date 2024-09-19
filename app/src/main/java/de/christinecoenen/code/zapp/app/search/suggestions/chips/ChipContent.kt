package de.christinecoenen.code.zapp.app.search.suggestions.chips

import androidx.annotation.DrawableRes

interface ChipContent {
	val content: Any

	val label: String

	@get:DrawableRes
	val icon: Int
}
