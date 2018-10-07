package de.christinecoenen.code.zapp.app.player;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.io.IOException;

import de.christinecoenen.code.zapp.R;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Transforms player events from exo player into RXJava observables.
 */
class PlayerEventHandler implements AnalyticsListener {

	private BehaviorSubject<Boolean> isBufferingSource = BehaviorSubject.create();
	private BehaviorSubject<Integer> errorResourceIdSource = BehaviorSubject.create();

	BehaviorSubject<Boolean> isBuffering() {
		return isBufferingSource;
	}

	BehaviorSubject<Integer> getErrorResourceId() {
		return errorResourceIdSource;
	}

	@Override
	public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {
		boolean isBuffering = playWhenReady && playbackState == Player.STATE_BUFFERING;
		isBufferingSource.onNext(isBuffering);
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
		}

		errorResourceIdSource.onNext(errorMessageResourceId);
	}

	@Override
	public void onLoadError(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
		// TODO: test with invalid stream url
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
