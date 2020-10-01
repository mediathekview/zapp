package de.christinecoenen.code.zapp.app.player

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData
import com.google.android.exoplayer2.upstream.HttpDataSource.HttpDataSourceException
import de.christinecoenen.code.zapp.R
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.IOException

/**
 * Transforms player events from exo player into RXJava observables.
 */
internal class PlayerEventHandler : AnalyticsListener {

	val isBuffering = BehaviorSubject.create<Boolean>()
	val isIdle = BehaviorSubject.create<Boolean>()
	val errorResourceId = BehaviorSubject.create<Int>()

	override fun onPlaybackStateChanged(eventTime: EventTime, playbackState: Int) {
		val isBuffering = playbackState == Player.STATE_BUFFERING
		this.isBuffering.onNext(isBuffering)

		val isReady = playbackState == Player.STATE_IDLE
		this.isIdle.onNext(isReady)
	}

	override fun onPlayerError(eventTime: EventTime, error: ExoPlaybackException) {
		val errorMessageResourceId =
			when (error.type) {
				ExoPlaybackException.TYPE_SOURCE -> {
					Timber.e(error, "exo player error TYPE_SOURCE")
					R.string.error_stream_io
				}
				ExoPlaybackException.TYPE_RENDERER -> {
					Timber.e(error, "exo player error TYPE_RENDERER")
					R.string.error_stream_unsupported
				}
				else -> {
					Timber.e(error, "exo player error %s", error.type)
					R.string.error_stream_unknown
				}
			}

		errorResourceId.onNext(errorMessageResourceId)
	}

	override fun onLoadError(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData, error: IOException, wasCanceled: Boolean) {
		if (wasCanceled) {
			return
		}

		Timber.e(error, "exo player onLoadError")

		if (error is HttpDataSourceException) {
			errorResourceId.onNext(R.string.error_stream_io)
		} else {
			errorResourceId.onNext(R.string.error_stream_unknown)
		}
	}
}
