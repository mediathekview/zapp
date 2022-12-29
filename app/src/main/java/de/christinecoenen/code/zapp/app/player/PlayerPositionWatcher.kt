package de.christinecoenen.code.zapp.app.player

import com.google.android.exoplayer2.Player
import java.time.Duration
import java.util.*
import kotlin.concurrent.timer

class PlayerPositionWatcher(
	val player: Player,
	val onPositionChanged: () -> Unit
) : Player.Listener {

	companion object {
		private const val TimerName = "PlayPositionTimer"
		private val TimerStartDelay = Duration.ofSeconds(1)
		private val TimerInterval = Duration.ofSeconds(1)
	}

	private var timer: Timer? = null

	init {
		player.addListener(this)
	}

	override fun onIsPlayingChanged(isPlaying: Boolean) {
		timer?.cancel()

		if (isPlaying) {
			timer = timer(TimerName, true, TimerStartDelay.toMillis(), TimerInterval.toMillis()) {
				onPositionChanged()
			}
		}
	}
}
