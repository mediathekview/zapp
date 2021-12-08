package de.christinecoenen.code.zapp.app.player

import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket

internal class TrackSelectorWrapper(private val trackSelector: DefaultTrackSelector) {

	companion object {
		private const val MEDIUM_QUALITY_MAX_VIDEO_WIDTH = 640
		private const val MEDIUM_QUALITY_MAX_VIDEO_HEIGHT = 360
	}

	fun setStreamQuality(streamQuality: StreamQualityBucket?) {

		when (streamQuality) {
			StreamQualityBucket.DISABLED ->
				throw IllegalArgumentException("track selection does not allow disabled wuality bucket")

			StreamQualityBucket.LOWEST ->
				trackSelector.setParameters(trackSelector
					.buildUponParameters()
					.clearVideoSizeConstraints()
					.setForceLowestBitrate(true))

			StreamQualityBucket.MEDIUM ->
				trackSelector.setParameters(trackSelector
					.buildUponParameters()
					.clearVideoSizeConstraints()
					.setMaxVideoSize(MEDIUM_QUALITY_MAX_VIDEO_WIDTH, MEDIUM_QUALITY_MAX_VIDEO_HEIGHT)
					.setForceLowestBitrate(false))

			StreamQualityBucket.HIGHEST ->
				trackSelector.setParameters(trackSelector
					.buildUponParameters()
					.clearVideoSizeConstraints()
					.setForceLowestBitrate(false))
		}
	}

}
