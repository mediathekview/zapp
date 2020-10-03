package de.christinecoenen.code.zapp.utils.video;

import android.view.View;

import com.google.android.exoplayer2.Player;

import java.lang.ref.WeakReference;

/**
 * Allows the screen to turn off as soon as video playback finishes
 * or pauses.
 */
class ScreenDimmingVideoEventListener implements Player.EventListener {

	private final WeakReference<View> viewToKeepScreenOn;

	ScreenDimmingVideoEventListener(View viewToKeepScreenOn) {
		this.viewToKeepScreenOn = new WeakReference<>(viewToKeepScreenOn);
	}

	@Override
	public void onPlaybackStateChanged(int playbackState) {
		View view = viewToKeepScreenOn.get();

		if (view == null) {
			return;
		}

		if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
			view.setKeepScreenOn(false);
		} else {
			// This prevents the screen from getting dim/lock
			view.setKeepScreenOn(true);
		}
	}
}
