package de.christinecoenen.code.zapp.utils.video;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import timber.log.Timber;


public class VideoBufferingHandler implements ExoPlayer.EventListener {

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
			Timber.v("media player buffering start");
			listener.onBufferingStarted();
		} else {
			Timber.v("media player buffering end");
			listener.onBufferingEnded();
		}
	}

	@Override
	public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
		if (playWhenReady && playbackState == SimpleExoPlayer.STATE_READY) {
			Timber.d("media player rendering start");
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
