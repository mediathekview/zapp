package de.christinecoenen.code.zapp.app.player

import android.text.TextUtils
import androidx.annotation.DrawableRes
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality
import java.io.Serializable

data class VideoInfo(
	var id: Int = 0,
	var title: String,
	var url: String,
	var urlLowestQuality: String? = null,
	var urlHighestQuality: String? = null,
	var subtitle: String? = null,
	var subtitleUrl: String? = null,
	var filePath: String? = null,
	var hasDuration: Boolean = false,
	@DrawableRes var artworkVectorDrawableResId: Int = 0
) : Serializable {

	companion object {

		@JvmStatic
		fun fromShow(persistedShow: PersistedMediathekShow): VideoInfo {
			val show = persistedShow.mediathekShow

			return VideoInfo(
				title = show.title,
				url = show.videoUrl
			).apply {
				id = persistedShow.id
				urlHighestQuality = show.getVideoUrl(Quality.High)
				urlLowestQuality = show.getVideoUrl(Quality.Low)
				filePath = persistedShow.downloadedVideoPath
				subtitle = show.topic
				subtitleUrl = show.subtitleUrl
				hasDuration = true
			}
		}

		@JvmStatic
		fun fromChannel(channel: ChannelModel): VideoInfo {
			return VideoInfo(
				title = channel.name,
				url = channel.streamUrl
			).apply {
				url = channel.streamUrl
				hasDuration = false
				artworkVectorDrawableResId = channel.drawableId
			}
		}

	}

	val hasSubtitles: Boolean
		// no subtitle support for downloaded videos yet
		get() = !TextUtils.isEmpty(subtitleUrl) && !isOfflineVideo

	val isOfflineVideo: Boolean
		get() = filePath != null

	val hasArtwork: Boolean
		get() = artworkVectorDrawableResId != 0

	fun getPlaybackUrlOrFilePath(quality: StreamQualityBucket): String {
		return if (isOfflineVideo) {
			filePath!!
		} else when (quality) {
			StreamQualityBucket.MEDIUM -> url
			StreamQualityBucket.HIGHEST -> urlHighestQuality ?: url
			else -> urlLowestQuality ?: url
		}
	}
}
