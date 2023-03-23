package de.christinecoenen.code.zapp.app.player

import android.os.Handler
import android.os.Looper
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SleepTimer(
	private val onTimerEnded: () -> Unit
) {

	private val handler = Handler(Looper.getMainLooper())
	private var timerEndTime: Instant? = null
	private var listener: Listener? = null

	private val timerEndedRunnable = Runnable {
		onTimerEnded.invoke()
		stop()
	}

	val isRunning: Boolean
		get() = timerEndTime != null

	val timeLeft: Duration
		get() = timerEndTime?.let {
			(it.toEpochMilli() - Instant.now().toEpochMilli()).milliseconds
		} ?: Duration.ZERO

	fun start(duration: Duration) {
		stop()
		timerEndTime = Instant.now().plusMillis(duration.inWholeMilliseconds)
		listener?.onIsRunningChanged(true)
		handler.postDelayed(timerEndedRunnable, duration.inWholeMilliseconds)
	}

	fun stop() {
		timerEndTime = null
		listener?.onIsRunningChanged(false)
		handler.removeCallbacks(timerEndedRunnable)
	}

	fun setListener(listener: Listener?) {
		this.listener = listener
		listener?.onIsRunningChanged(isRunning)
	}

	interface Listener {
		fun onIsRunningChanged(isRunning: Boolean)
	}
}
