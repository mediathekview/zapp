package de.christinecoenen.code.zapp.app.mediathek.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class PersistedMediathekShow {

	@PrimaryKey(autoGenerate = true)
	var id : Int = 0

	var downloadId = 0

	var downloadedVideoPath: String? = null

	var downloadStatus : DownloadStatus = DownloadStatus.NONE

	var downloadProgress = 0

	var playbackPosition = 0L

	@Embedded
	var mediathekShow: MediathekShow = MediathekShow()

}
