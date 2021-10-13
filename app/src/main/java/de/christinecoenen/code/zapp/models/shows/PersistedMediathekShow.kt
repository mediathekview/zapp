package de.christinecoenen.code.zapp.models.shows

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.joda.time.DateTime

/**
 * [MediathekShow] persisted to the database, because it has been accessed by the user in any way.
 */
@Entity(indices = [Index(value = ["apiId"], unique = true)])
data class PersistedMediathekShow(

	@PrimaryKey(autoGenerate = true)
	var id: Int = 0,

	var createdAt: DateTime = DateTime.now(),

	/**
	 * Id used for download handling only - should not be used in any interface.
	 */
	var downloadId: Int = 0,

	var downloadedAt: DateTime? = null,

	var downloadedVideoPath: String? = null,

	var downloadStatus: DownloadStatus = DownloadStatus.NONE,

	var downloadProgress: Int = 0,

	var lastPlayedBackAt: DateTime? = null,

	var playbackPosition: Long = 0,

	var videoDuration: Long = 0,

	@Embedded
	var mediathekShow: MediathekShow

) {

	val playBackPercent
		get() = (playbackPosition.toFloat() / videoDuration).let { if (it.isNaN()) 0f else it }

}
