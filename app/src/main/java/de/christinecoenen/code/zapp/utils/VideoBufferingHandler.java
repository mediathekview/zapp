package de.christinecoenen.code.zapp.utils;

import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;


public class VideoBufferingHandler implements ExoPlayer.EventListener {

	private static final String TAG = VideoBufferingHandler.class.getSimpleName();

	private final IVideoBufferingListener listener;

	public VideoBufferingHandler(IVideoBufferingListener listener) {
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
		if (isLoading) {
			Log.d(TAG, "media player buffering start");
			listener.onBufferingStarted();
		} else {
			Log.d(TAG, "media player buffering end");
			listener.onBufferingEnded();
		}
	}

	@Override
	public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
		if (playWhenReady && playbackState == SimpleExoPlayer.STATE_READY) {
			Log.d(TAG, "media player rendering start");
			listener.onBufferingEnded();
		}
	}

	@Override
	public void onPlayerError(ExoPlaybackException error) {
	}

	@Override
	public void onPositionDiscontinuity() {
	}

	@Override
	public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
	}

	public interface IVideoBufferingListener {
		void onBufferingStarted();

		void onBufferingEnded();
	}
}
