package de.christinecoenen.code.zapp.models.shows

import android.content.Context
import android.text.TextUtils
import android.text.format.DateUtils
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.christinecoenen.code.zapp.utils.system.IntentHelper
import de.christinecoenen.code.zapp.utils.view.ShowDurationFormatter
import org.joda.time.DateTimeZone
import java.io.File
import java.io.Serializable

@Keep
data class MediathekShow(
	@SerializedName("id")
	val apiId: String,
	val topic: String = "",
	val title: String,
	val description: String? = null,
	val channel: String,
	val timestamp: Int = 0,
	val size: Long = 0,
	val duration: String? = null,
	val filmlisteTimestamp: Int = 0,

	@SerializedName("url_website")
	val websiteUrl: String? = null,

	@SerializedName("url_subtitle")
	val subtitleUrl: String? = null,

	@SerializedName("url_video")
	val videoUrl: String,

	@SerializedName("url_video_low")
	val videoUrlLow: String? = null,

	@SerializedName("url_video_hd")
	val videoUrlHd: String? = null,
) : Serializable {

	val formattedTimestamp: CharSequence
		get() {
			if (timestamp == 0) {
				return "?"
			}

			val time = DateTimeZone
				.forID("Europe/Berlin")
				.convertLocalToUTC(timestamp * DateUtils.SECOND_IN_MILLIS, false)

			return DateUtils.getRelativeTimeSpanString(time)
		}

	val formattedDuration: String
		get() {
			if (duration == null) {
				return "?"
			}

			val duration: Int = try {
				this.duration.toInt()
			} catch (e: NumberFormatException) {
				return "?"
			}

			return ShowDurationFormatter.formatSeconds(duration)
		}

	val hasSubtitle
		get() = !TextUtils.isEmpty(subtitleUrl)

	val supportedDownloadQualities: List<Quality>
		get() = Quality.values().filter(::hasDownloadQuality)

	val supportedStreamingQualities
		get() = Quality.values().filter(::hasStreamingQuality)

	fun getVideoUrl(quality: Quality): String? {
		return when (quality) {
			Quality.Low -> if (videoUrlLow.isNullOrEmpty()) null else videoUrlLow
			Quality.Medium -> videoUrl
			Quality.High -> if (videoUrlHd.isNullOrEmpty()) null else videoUrlHd
		}
	}

	fun hasAnyDownloadQuality(): Boolean {
		val highVideoUrl = getVideoUrl(Quality.High)
		val mediumVideoUrl = getVideoUrl(Quality.Medium)
		return isValidDownloadUrl(highVideoUrl) || isValidDownloadUrl(mediumVideoUrl)
	}

	private fun hasDownloadQuality(quality: Quality): Boolean {
		val videoUrl = getVideoUrl(quality)
		return isValidDownloadUrl(videoUrl)
	}

	private fun hasStreamingQuality(quality: Quality): Boolean {
		val videoUrl = getVideoUrl(quality)
		return isValidStreamingUrl(videoUrl)
	}

	fun getDownloadFileName(quality: Quality): String {
		val videoUrl = getVideoUrl(quality)
			?: throw IllegalArgumentException("No download file available for quality $quality.")
		return getDownloadFileName(videoUrl)
	}

	/**
	 * Shares this shows url to other apps in the Android system.
	 *
	 * @param quality Quality to share. Default is highest available quality.
	 * Non existing qualities will fall back to existing ones.
	 */
	fun shareExternally(context: Context, quality: Quality = Quality.High) {
		val url = getVideoUrl(quality) ?: videoUrl
		IntentHelper.shareLink(context, url, "$topic - $title")
	}

	/**
	 * Shares this shows url to other video player or editor apps in the Android system.
	 *
	 * @param quality Quality to share. Default is highest available quality.
	 * Non existing qualities will fall back to existing ones.
	 */
	fun playExternally(context: Context, quality: Quality = Quality.High) {
		val url = getVideoUrl(quality) ?: videoUrl
		IntentHelper.playVideo(context, url, "$topic - $title")
	}

	private fun getDownloadFileName(videoUrl: String): String {
		val extension = File(videoUrl).extension

		// needed for samsung devices
		val maxFileNameLength = 120
		var fileName =
			if (title.length <= maxFileNameLength) title else title.substring(0, maxFileNameLength)

		// replace characters that may crash download manager
		fileName = fileName.replace("[\\\\/:*?\"<>|%]".toRegex(), "-")
		fileName = fileName.replace("\\.\\.\\.".toRegex(), "…")

		return "$fileName.$extension"
	}

	private fun isValidStreamingUrl(url: String?): Boolean {
		return !TextUtils.isEmpty(url)
	}

	private fun isValidDownloadUrl(url: String?): Boolean {
		return !TextUtils.isEmpty(url) && !url!!.endsWith("m3u8") && !url.endsWith("csmil")
	}
}
