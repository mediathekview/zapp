package de.christinecoenen.code.zapp.persistence

import androidx.room.TypeConverter
import de.christinecoenen.code.zapp.models.shows.DownloadStatus


class DownloadStatusConverter {

	companion object {

		@TypeConverter
		@JvmStatic
		fun fromDownloadStatus(value: DownloadStatus): Int {
			return value.ordinal
		}

		@TypeConverter
		@JvmStatic
		fun toDownloadStatus(value: Int): DownloadStatus {
			return DownloadStatus.entries[value]
		}

	}
}
