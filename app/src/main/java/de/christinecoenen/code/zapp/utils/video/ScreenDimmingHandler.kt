package de.christinecoenen.code.zapp.utils.video

import android.view.View
import androidx.media3.common.Player
import java.lang.ref.WeakReference

/**
 * Allows the screen to turn off as soon as video playback finishes
 * or pauses.
 */
internal class ScreenDimmingHandler : Player.Listener {

	private var viewToKeepScreenOn: WeakReference<View?> = WeakReference(null)

	private var isIdleOrEnded = false
	private var isPaused = false

	/**
	 * The given screen will only be kept on as long as any video is playing.
	 */
	fun setScreenToKeepOn(view: View) {
		viewToKeepScreenOn = WeakReference(view)
		applyScreenDimming()
	}

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
