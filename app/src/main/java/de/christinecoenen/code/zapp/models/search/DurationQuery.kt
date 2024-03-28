package de.christinecoenen.code.zapp.models.search

import de.christinecoenen.code.zapp.utils.view.ShowDurationFormatter

data class DurationQuery(
	val comparison: Comparison,
	val minutes: Int,
) {
	val label = when (comparison) {
		Comparison.LesserThan -> "< " + ShowDurationFormatter.formatMinutes(minutes)
		Comparison.GreaterThan -> "> " + ShowDurationFormatter.formatMinutes(minutes)
	}
}
