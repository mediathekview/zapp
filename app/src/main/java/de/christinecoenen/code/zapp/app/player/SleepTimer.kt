package de.christinecoenen.code.zapp.app.player

import android.os.Handler
import android.os.Looper
import java.time.Duration
import java.time.LocalDateTime

class SleepTimer(
	private val onTimerEnded: () -> Unit
) {

	private val handler = Handler(Looper.getMainLooper())
	private var timerEndTime: LocalDateTime? = null
	private var listener: Listener? = null

	private val timerEndedRunnable = Runnable {
		onTimerEnded.invoke()
		stop()
	}

	val isRunning: Boolean
		get() = timerEndTime != null

	val timeLeft: Duration
		get() = Duration.between(LocalDateTime.now(), timerEndTime)

	fun start(duration: Duration) {
		stop()
		timerEndTime = LocalDateTime.now().plus(duration)
		listener?.onIsRunningChanged(true)
		handler.postDelayed(timerEndedRunnable, duration.toMillis())
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
