package de.christinecoenen.code.zapp.app.mediathek.ui.list.models

data class LengthFilter(
	var minDurationSeconds: Int = 0,
	var maxDurationSeconds: Int? = null
) {
	val isApplied: Boolean
		get() = minDurationSeconds != 0 || maxDurationSeconds != null
}
