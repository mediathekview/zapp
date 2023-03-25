package de.christinecoenen.code.zapp.app.player

import android.os.Handler
import android.os.Looper
import java.time.Instant
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SleepTimer(
	private val onTimerEnded: () -> Unit
) {

	private val handler = Handler(Looper.getMainLooper())
	private var timerEndTime: Instant? = null
	private val listeners: WeakHashMap<Listener, Void> = WeakHashMap()

	private val timerAlmostEndedRunnable = Runnable {
		onTimerAlmostEnded()
	}

	private val timerEndedRunnable = Runnable {
		onTimerEnded()
		stop()
	}

	val isRunning: Boolean
		get() = timerEndTime != null

	val timeLeft: Duration
		get() = timerEndTime?.let {
			(it.toEpochMilli() - Instant.now().toEpochMilli()).milliseconds
		} ?: Duration.ZERO

	fun addTime(duration: Duration) {
		if (!isRunning) {
			throw IllegalStateException()
		}

		val newDuration = timeLeft.plus(duration)
		stop()
		start(newDuration)
	}

	fun start(duration: Duration) {
		stop()

		timerEndTime = Instant.now().plusMillis(duration.inWholeMilliseconds)
		onIsRunningChanged()

		val almostEndedDuration = duration.minus(10.seconds)
		handler.postDelayed(timerAlmostEndedRunnable, almostEndedDuration.inWholeMilliseconds)
		handler.postDelayed(timerEndedRunnable, duration.inWholeMilliseconds)
	}

	fun stop() {
		timerEndTime = null
		onIsRunningChanged()
		handler.removeCallbacks(timerAlmostEndedRunnable)
		handler.removeCallbacks(timerEndedRunnable)
	}

	fun addListener(listener: Listener) {
		listeners[listener] = null
		listener.onIsRunningChanged(isRunning)
	}

	fun removeListener(listener: Listener) {
		listeners.remove(listener)
	}

	private fun onIsRunningChanged() {
		listeners.keys.forEach { it.onIsRunningChanged(isRunning) }
	}

	private fun onTimerAlmostEnded() {
		listeners.keys.forEach { it.onTimerAlmostEnded() }
	}

	private fun onTimerEnded() {
		onTimerEnded.invoke()
		listeners.keys.forEach { it.onTimerEnded() }
	}

	interface Listener {
		fun onIsRunningChanged(isRunning: Boolean) {}
		fun onTimerAlmostEnded() {}
		fun onTimerEnded() {}
	}
}
