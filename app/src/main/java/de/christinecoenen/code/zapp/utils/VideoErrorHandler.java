package de.christinecoenen.code.zapp.utils;

import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.io.IOException;

import de.christinecoenen.code.zapp.R;


public class VideoErrorHandler implements ExoPlayer.EventListener, AdaptiveMediaSourceEventListener {

	private static final String TAG = VideoErrorHandler.class.getSimpleName();

	private final IVideoErrorListener listener;

	public VideoErrorHandler(IVideoErrorListener listener) {
		this.listener = listener;
	}

	@Override
	public void onTimelineChanged(Timeline timeline, Object manifest) {

	}

	@Override
	public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

	}

	@Override
	public void onLoadingChanged(boolean isLoading) {

	}

	@Override
	public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

	}

	@Override
	public void onPlayerError(ExoPlaybackException error) {
		int errorMessageResourceId = R.string.error_stream_unknown;

		switch (error.type) {
			case ExoPlaybackException.TYPE_SOURCE:
				Log.e(TAG, "exo player error TYPE_SOURCE: " + error.getSourceException().getMessage());
				errorMessageResourceId = R.string.error_stream_io;
				break;
			case ExoPlaybackException.TYPE_RENDERER:
				Log.e(TAG, "exo player error TYPE_RENDERER: " + error.getRendererException().getMessage());
				errorMessageResourceId = R.string.error_stream_unsupported;
				break;
			case ExoPlaybackException.TYPE_UNEXPECTED:
				Log.e(TAG, "exo player error TYPE_UNEXPECTED: " + error.getUnexpectedException().getMessage());
				break;
		}

		listener.onVideoError(errorMessageResourceId);
	}

	@Override
	public void onPositionDiscontinuity() {

	}

	@Override
	public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

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

	public interface IVideoErrorListener {
		void onVideoError(int messageResourceId);
	}
}
