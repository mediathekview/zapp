package de.christinecoenen.code.zapp.utils.video;

import com.google.android.exoplayer2.Player;

import timber.log.Timber;


public class VideoBufferingHandler extends Player.DefaultEventListener {

	private final IVideoBufferingListener listener;

	public VideoBufferingHandler(IVideoBufferingListener listener) {
		this.listener = listener;
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
		if (playWhenReady && playbackState == Player.STATE_READY) {
			Timber.d("media player rendering start");
			listener.onBufferingEnded();
		} else if (playbackState == Player.STATE_ENDED) {
			Timber.d("media video ended");
			listener.onVideoEnded();
		}
	}

	public interface IVideoBufferingListener {
		void onBufferingStarted();

		void onBufferingEnded();

		void onVideoEnded();
	}
}
