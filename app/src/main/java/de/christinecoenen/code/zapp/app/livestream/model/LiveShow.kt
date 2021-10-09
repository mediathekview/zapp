package de.christinecoenen.code.zapp.app.livestream.model

import org.joda.time.DateTime
import org.joda.time.Duration

data class LiveShow(
	var title: String,
	var subtitle: String? = null,
	var description: String? = null,
	var startTime: DateTime? = null,
	var endTime: DateTime? = null
) {

	val progressPercent: Float
		get() {
			val showDuration = Duration(startTime, endTime)
			val runningDuration = Duration(startTime, DateTime.now())
			return runningDuration.standardSeconds.toFloat() / showDuration.standardSeconds
		}

	fun hasDuration(): Boolean {
		return startTime != null && endTime != null
	}

}
