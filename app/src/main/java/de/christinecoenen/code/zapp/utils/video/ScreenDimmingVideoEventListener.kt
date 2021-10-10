package de.christinecoenen.code.zapp.utils.video

import android.view.View
import com.google.android.exoplayer2.Player
import java.lang.ref.WeakReference

/**
 * Allows the screen to turn off as soon as video playback finishes
 * or pauses.
 */
internal class ScreenDimmingVideoEventListener(viewToKeepScreenOn: View) : Player.Listener {

	private val viewToKeepScreenOn: WeakReference<View> = WeakReference(viewToKeepScreenOn)

	private var isIdleOrEnded = false
	private var isPaused = false

	override fun onPlaybackStateChanged(playbackState: Int) {
		isIdleOrEnded = playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED
		applyScreenDimming()
	}

	override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
		isPaused = !playWhenReady
		applyScreenDimming()
	}

	private fun applyScreenDimming() {
		viewToKeepScreenOn.get()?.keepScreenOn = !(isPaused || isIdleOrEnded)
	}
}
