package de.christinecoenen.code.zapp.app.player;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.LoadEventInfo;
import com.google.android.exoplayer2.source.MediaLoadData;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.io.IOException;

import de.christinecoenen.code.zapp.R;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Transforms player events from exo player into RXJava observables.
 */
class PlayerEventHandler implements AnalyticsListener {

	private final BehaviorSubject<Boolean> isBufferingSource = BehaviorSubject.create();
	private final BehaviorSubject<Boolean> isIdleSource = BehaviorSubject.create();
	private final BehaviorSubject<Integer> errorResourceIdSource = BehaviorSubject.create();

	BehaviorSubject<Boolean> isBuffering() {
		return isBufferingSource;
	}

	BehaviorSubject<Boolean> isIdle() {
		return isIdleSource;
	}

	BehaviorSubject<Integer> getErrorResourceId() {
		return errorResourceIdSource;
	}

	@Override
	public void onPlaybackStateChanged(EventTime eventTime, int playbackState) {
		boolean isBuffering = playbackState == Player.STATE_BUFFERING;
		isBufferingSource.onNext(isBuffering);

		boolean isReady = playbackState == Player.STATE_IDLE;
		isIdleSource.onNext(isReady);
	}

	@Override
	public void onPlayerError(EventTime eventTime, ExoPlaybackException error) {
		int errorMessageResourceId = R.string.error_stream_unknown;

		switch (error.type) {
			case ExoPlaybackException.TYPE_SOURCE:
				Timber.e(error, "exo player error TYPE_SOURCE");
				errorMessageResourceId = R.string.error_stream_io;
				break;
			case ExoPlaybackException.TYPE_RENDERER:
				Timber.e(error, "exo player error TYPE_RENDERER");
				errorMessageResourceId = R.string.error_stream_unsupported;
				break;
			case ExoPlaybackException.TYPE_UNEXPECTED:
				Timber.e(error, "exo player error TYPE_UNEXPECTED");
				break;
			case ExoPlaybackException.TYPE_OUT_OF_MEMORY:
			case ExoPlaybackException.TYPE_REMOTE:
			case ExoPlaybackException.TYPE_TIMEOUT:
				Timber.e(error, "exo player error %s", error.type);
				break;
		}

		errorResourceIdSource.onNext(errorMessageResourceId);
	}

	@Override
	public void onLoadError(EventTime eventTime, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
		if (wasCanceled) {
			return;
		}

		Timber.e(error, "exo player onLoadError");

		if (error instanceof HttpDataSource.HttpDataSourceException) {
			errorResourceIdSource.onNext(R.string.error_stream_io);
		} else {
			errorResourceIdSource.onNext(R.string.error_stream_unknown);
		}
	}
}
