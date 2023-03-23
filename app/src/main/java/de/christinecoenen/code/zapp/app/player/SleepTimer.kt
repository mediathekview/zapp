package de.christinecoenen.code.zapp.app.player

import android.os.Handler
import android.os.Looper
import java.time.Duration
import java.util.*

class SleepTimer(
	private val onTimerEnded: () -> Unit
) {

	private val handler = Handler(Looper.getMainLooper())
	private var timerStartTime: Date? = null

	private val timerEndedRunnable = Runnable {
		onTimerEnded.invoke()
	}

	val isRunning: Boolean
		get() = timerStartTime != null

	fun start(duration: Duration) {
		stop()
		timerStartTime = Date()
		handler.postDelayed(timerEndedRunnable, duration.toMillis())
	}

	fun stop() {
		timerStartTime = null
		handler.removeCallbacks(timerEndedRunnable)
	}
}
