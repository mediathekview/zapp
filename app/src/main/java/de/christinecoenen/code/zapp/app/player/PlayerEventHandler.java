package de.christinecoenen.code.zapp.app.player;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.AnalyticsListener;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Transforms player events from exo player into RXJava observables.
 */
class PlayerEventHandler implements AnalyticsListener {

	private BehaviorSubject<Boolean> isBufferingSource = BehaviorSubject.create();

	Observable<Boolean> isBuffering() {
		return isBufferingSource;
	}

	@Override
	public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {
		boolean isBuffering = playWhenReady && playbackState == Player.STATE_BUFFERING;
		isBufferingSource.onNext(isBuffering);
	}
}
