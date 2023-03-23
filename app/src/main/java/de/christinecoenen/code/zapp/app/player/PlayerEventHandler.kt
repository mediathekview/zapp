package de.christinecoenen.code.zapp.app.player

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.HttpDataSource.HttpDataSourceException
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import de.christinecoenen.code.zapp.R
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.io.IOException

/**
 * Transforms player events from exo player into RXJava observables.
 */
@androidx.media3.common.util.UnstableApi
internal class PlayerEventHandler : AnalyticsListener {

	val isIdle = MutableStateFlow(false)
	val errorResourceId = MutableStateFlow(0)

	override fun onPlaybackStateChanged(eventTime: EventTime, playbackState: Int) {
		val isReady = playbackState == Player.STATE_IDLE
		this.isIdle.tryEmit(isReady)
	}

	override fun onPlayerError(eventTime: EventTime, error: PlaybackException) {
		val errorMessageResourceId =
			when (error.errorCode) {
				PlaybackException.ERROR_CODE_IO_NO_PERMISSION,
				PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
					Timber.e(error, "exo player error %s", error.errorCodeName)
					R.string.error_stream_no_file_access
				}
				PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
					Timber.e(error, "exo player error %s", error.errorCodeName)
					R.string.error_stream_no_bad_http_status
				}
				in PlaybackException.ERROR_CODE_IO_UNSPECIFIED until
					PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED -> {
					Timber.e(error, "exo player error %s", error.errorCodeName)
					R.string.error_stream_io
				}
				in PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED until
					PlaybackException.ERROR_CODE_DRM_UNSPECIFIED -> {
					Timber.e(error, "exo player error %s", error.errorCodeName)
					R.string.error_stream_unsupported
				}
				else -> {
					Timber.e(error, "exo player error %s", error.errorCodeName)
					R.string.error_stream_unknown
				}
			}

		this.errorResourceId.tryEmit(errorMessageResourceId)
	}

	override fun onLoadError(
		eventTime: EventTime,
		loadEventInfo: LoadEventInfo,
		mediaLoadData: MediaLoadData,
		error: IOException,
		wasCanceled: Boolean
	) {
		if (wasCanceled) {
			return
		}

		Timber.e(error, "exo player onLoadError")

		if (error is HttpDataSourceException) {
			this.errorResourceId.tryEmit(R.string.error_stream_io)
		} else {
			this.errorResourceId.tryEmit(R.string.error_stream_unknown)
		}
	}
}
