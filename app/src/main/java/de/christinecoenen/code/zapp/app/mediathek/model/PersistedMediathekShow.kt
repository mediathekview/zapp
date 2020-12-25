package de.christinecoenen.code.zapp.app.mediathek.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity(indices = [Index(value = ["apiId"], unique = true)])
data class PersistedMediathekShow(

	@PrimaryKey(autoGenerate = true)
	var id: Int = 0,

	var createdAt: DateTime = DateTime.now(),

	var downloadId: Int = 0,

	var downloadedAt: DateTime? = null,

	var downloadedVideoPath: String? = null,

	var downloadStatus: DownloadStatus = DownloadStatus.NONE,

	var downloadProgress: Int = 0,

	var lastPlayedBackAt: DateTime? = null,

	var playbackPosition: Long = 0,

	var videoDuration: Long = 0,

	@Embedded
	var mediathekShow: MediathekShow = MediathekShow()

) {

	val playBackPercent
		get() = playbackPosition.toFloat() / videoDuration

}
