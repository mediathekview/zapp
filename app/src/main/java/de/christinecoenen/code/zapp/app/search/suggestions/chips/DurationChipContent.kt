package de.christinecoenen.code.zapp.app.search.suggestions.chips

import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.search.DurationQuery

data class DurationChipContent(
	val durationQuery: DurationQuery,
) : ChipContent {

	override val content = durationQuery
	override val label: String = durationQuery.label
	override val icon = R.drawable.outline_timelapse_24

}
