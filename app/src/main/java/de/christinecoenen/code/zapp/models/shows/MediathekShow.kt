package de.christinecoenen.code.zapp.models.shows

import android.content.Intent
import android.text.TextUtils
import android.text.format.DateUtils
import com.google.gson.annotations.SerializedName
import org.apache.commons.io.FilenameUtils
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder
import java.io.Serializable

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

			val period = Duration.standardSeconds(duration.toLong()).toPeriod()
			val formatter = if (period.hours > 0) hourPeriodFormatter else secondsPeriodFormatter

			return period.toString(formatter)
		}

	val hasSubtitle
		get() = !TextUtils.isEmpty(subtitleUrl)

	val shareIntentPlain: Intent
		get() = Intent(Intent.ACTION_SEND).apply {
			type = "text/plain"
			putExtra(Intent.EXTRA_SUBJECT, "$topic - $title")
			putExtra(Intent.EXTRA_TEXT, videoUrl)
		}

	val supportedDownloadQualities: List<Quality>
		get() = Quality.values().filter(::hasDownloadQuality)

	val supportedStreamingQualities
		get() = Quality.values().filter(::hasStreamingQuality)

	fun getVideoUrl(quality: Quality): String? {
		return when (quality) {
			Quality.Low -> videoUrlLow
			Quality.Medium -> videoUrl
			Quality.High -> videoUrlHd
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
		return getDownloadFileName(videoUrl)
	}

	private fun getDownloadFileName(videoUrl: String?): String {
		val extension = FilenameUtils.getExtension(videoUrl)

		// needed for samsung devices
		val maxFileNameLength = 120
		var fileName = if (title.length <= maxFileNameLength) title else title.substring(0, maxFileNameLength)

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

	companion object {

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

	}
}
