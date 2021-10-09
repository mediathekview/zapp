package de.christinecoenen.code.zapp.app.livestream.api.model

import androidx.annotation.Keep
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import org.joda.time.format.ISODateTimeFormat

@Keep
data class Show(
	val title: String,
	val subtitle: String? = null,
	val description: String? = null,
	val startTime: String? = null,
	val endTime: String? = null,
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
