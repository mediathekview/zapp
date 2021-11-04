package de.christinecoenen.code.zapp.utils.view

import org.joda.time.Duration
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder

object ShowDurationFormatter {

	private val hourPeriodFormatter = PeriodFormatterBuilder()
		.appendHours()
		.appendSeparatorIfFieldsBefore("h ")
		.appendMinutes()
		.appendSeparatorIfFieldsBefore("m")
		.toFormatter()

	private val secondsPeriodFormatter = PeriodFormatterBuilder()
		.appendMinutes()
		.appendSeparatorIfFieldsBefore("m ")
		.appendSeconds()
		.appendSeparatorIfFieldsBefore("s")
		.toFormatter()

	fun formatMinutes(duration: Int): String {
		val period = Duration.standardMinutes(duration.toLong()).toPeriod()
		return format(period)
	}

	fun formatSeconds(duration: Int): String {
		val period = Duration.standardSeconds(duration.toLong()).toPeriod()
		return format(period)
	}

	private fun format(period: Period): String {
		val formatter = if (period.hours > 0) hourPeriodFormatter else secondsPeriodFormatter
		return period.toString(formatter)
	}
}
