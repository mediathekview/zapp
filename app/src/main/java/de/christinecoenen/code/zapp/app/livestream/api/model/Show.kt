package de.christinecoenen.code.zapp.app.livestream.api.model

import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import org.joda.time.format.ISODateTimeFormat

data class Show(
	private val title: String,
	private val subtitle: String? = null,
	private val description: String? = null,
	private val startTime: String? = null,
	private val endTime: String? = null,
) {

	companion object {
		private val formatter = ISODateTimeFormat.dateTimeParser()
	}

	fun toLiveShow(): LiveShow {
		val liveShow = LiveShow(
			title = title,
			subtitle = subtitle,
			description = description
		)

		if (startTime != null && endTime != null) {
			liveShow.startTime = formatter.parseDateTime(startTime)
			liveShow.endTime = formatter.parseDateTime(endTime)
		}

		return liveShow
	}
}
