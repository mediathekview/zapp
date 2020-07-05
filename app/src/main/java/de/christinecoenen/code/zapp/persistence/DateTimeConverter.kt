package de.christinecoenen.code.zapp.persistence

import androidx.room.TypeConverter
import org.joda.time.DateTime


class DateTimeConverter {

	companion object {

		@TypeConverter
		@JvmStatic
		fun fromDownloadStatus(value: DateTime): Long {
			return value.toDateTimeISO().millis
		}

		@TypeConverter
		@JvmStatic
		fun toDownloadStatus(value: Long): DateTime {
			return DateTime(value)
		}

	}
}
