package de.christinecoenen.code.zapp.utils.video

import android.view.View
import com.google.android.exoplayer2.Player
import java.lang.ref.WeakReference

/**
 * Allows the screen to turn off as soon as video playback finishes
 * or pauses.
 */
internal class ScreenDimmingVideoEventListener(viewToKeepScreenOn: View) : Player.EventListener {

	private val viewToKeepScreenOn: WeakReference<View> = WeakReference(viewToKeepScreenOn)

	override fun onPlaybackStateChanged(playbackState: Int) {
		// This prevents the screen from getting dim/lock
		viewToKeepScreenOn.get()?.keepScreenOn =
			playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED
	}

}
