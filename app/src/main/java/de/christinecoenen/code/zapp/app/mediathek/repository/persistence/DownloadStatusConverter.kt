package de.christinecoenen.code.zapp.app.mediathek.repository.persistence

import androidx.room.TypeConverter
import de.christinecoenen.code.zapp.app.mediathek.model.DownloadStatus


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
			return DownloadStatus.values()[value]
		}

	}
}
