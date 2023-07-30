package de.christinecoenen.code.zapp.app.mediathek.ui.list.filter.models

data class LengthFilter(
	var minDurationSeconds: Int = 0,
	var maxDurationSeconds: Int? = null
) {
	val isApplied: Boolean
		get() = minDurationSeconds != 0 || maxDurationSeconds != null

	val minDurationMinutes: Float
		get() = minDurationSeconds / 60f

	val maxDurationMinutes: Float?
		get() {
			val maxDuration = maxDurationSeconds
			return if (maxDuration == null) null else maxDuration / 60f
		}
}
