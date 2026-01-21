package de.christinecoenen.code.zapp.app.livestream.model

import android.content.Context
import de.christinecoenen.code.zapp.R
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import org.koin.android.ext.koin.androidContext

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

	val hasDuration
		get() = startTime != null && endTime != null

	fun formattedDuration(context: Context): String? {
		return if (hasDuration) {
			val startTime = startTime!!.toString(DateTimeFormat.shortTime())
			val endTime = endTime!!.toString(DateTimeFormat.shortTime())
			context.getString(R.string.view_program_info_show_time, startTime, endTime)
		} else {
			null
		}
	}

	companion object {
		fun getEmpty(context: Context): LiveShow {
			return LiveShow(context.getString(R.string.activity_channel_detail_info_error))
		}
	}
}
