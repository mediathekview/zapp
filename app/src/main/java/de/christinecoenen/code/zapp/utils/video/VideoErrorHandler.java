package de.christinecoenen.code.zapp.utils.video;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.io.IOException;

import de.christinecoenen.code.zapp.R;
import timber.log.Timber;


public class VideoErrorHandler extends Player.DefaultEventListener implements MediaSourceEventListener {

	private final IVideoErrorListener listener;

	public VideoErrorHandler(IVideoErrorListener listener) {
		this.listener = listener;
	}

	@Override
	public void onPlayerError(ExoPlaybackException error) {
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

		listener.onVideoError(errorMessageResourceId);
	}

	@Override
	public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {

	}

	@Override
	public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

	}

	@Override
	public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

	}

	@Override
	public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {
		// TODO: test with invalid stream url
		if (!wasCanceled) {
			Timber.e(error, "exo player onLoadError");

			if (error instanceof HttpDataSource.HttpDataSourceException) {
				listener.onVideoError(R.string.error_stream_io);
			} else {
				listener.onVideoError(R.string.error_stream_unknown);
			}
		}
	}

	@Override
	public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {

	}

	@Override
	public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {

	}

	public void onWrongNetworkError() {
		listener.onVideoError(R.string.error_stream_not_in_wifi);
	}

	public void onWrongNetworkErrorInvalid() {
		listener.onVideoErrorInvalid();
	}

	public interface IVideoErrorListener {
		void onVideoError(int messageResourceId);

		void onVideoErrorInvalid();
	}
}
