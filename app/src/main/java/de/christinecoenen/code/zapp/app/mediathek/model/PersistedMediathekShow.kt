package de.christinecoenen.code.zapp.app.mediathek.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity(indices = [Index(value = ["apiId"], unique = true)])
class PersistedMediathekShow {

	@PrimaryKey(autoGenerate = true)
	var id: Int = 0

	var createdAt: DateTime = DateTime.now()

	var downloadId = 0

	var downloadedAt: DateTime? = null

	var downloadedVideoPath: String? = null

	var downloadStatus: DownloadStatus = DownloadStatus.NONE

	var downloadProgress = 0

	var lastPlayedBackAt: DateTime? = null

	var playbackPosition = 0L

	var videoDuration = 0L

	@Embedded
	var mediathekShow: MediathekShow = MediathekShow()

}
