package de.christinecoenen.code.zapp.app.player

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.databinding.BottomSheetSleepTimerBinding
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SleepTimerBottomSheet : BottomSheetDialogFragment(), SleepTimer.Listener {

	private val settingsRepository: SettingsRepository by inject()

	private var _binding: BottomSheetSleepTimerBinding? = null
	private val binding: BottomSheetSleepTimerBinding get() = _binding!!

	private var tickTimer: Timer? = null
	private var sleepTimer: SleepTimer? = null

	private val backgroundPlayerServiceConnection: ServiceConnection = object : ServiceConnection {

		override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
			val binder = service as BackgroundPlayerService.Binder

			binder.getPlayer().sleepTimer.let {
				sleepTimer = it
				it.addListener(this@SleepTimerBottomSheet)
			}
		}

		override fun onServiceDisconnected(componentName: ComponentName) {
			sleepTimer?.removeListener(this@SleepTimerBottomSheet)
		}
	}

	fun expand() {
		(dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = BottomSheetSleepTimerBinding.inflate(inflater, container, false)

		binding.timerDelay.setText(settingsRepository.sleepTimerDelay.inWholeMinutes.toString())
		binding.timerDelay.setOnEditorActionListener { _, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				onStartClick()
			}
			false
		}

		binding.startButton.setOnClickListener { onStartClick() }
		binding.stopButton.setOnClickListener { onStopClick() }
		binding.addButton5Minutes.setOnClickListener { onAddClicked(5.minutes) }
		binding.addButton10Minutes.setOnClickListener { onAddClicked(10.minutes) }
		binding.addButton20Minutes.setOnClickListener { onAddClicked(20.minutes) }
		binding.setButton15Minutes.setOnClickListener { onSetClicked(15.minutes) }
		binding.setButton30Minutes.setOnClickListener { onSetClicked(30.minutes) }
		binding.setButton60Minutes.setOnClickListener { onSetClicked(60.minutes) }

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		BackgroundPlayerService.bind(requireContext(), backgroundPlayerServiceConnection)
		expand()
	}

	override fun onDestroyView() {
		sleepTimer?.removeListener(this)
		tickTimer?.cancel()

		requireContext().unbindService(backgroundPlayerServiceConnection)
		_binding = null

		super.onDestroyView()
	}

	private fun onStartClick() {
		val minutes = binding.timerDelay.text.toString().toLongOrNull()

		if (minutes == null) {
			sleepTimer?.stop()
		} else {
			settingsRepository.sleepTimerDelay = minutes.minutes
			sleepTimer?.start(settingsRepository.sleepTimerDelay)
		}
	}

	private fun onStopClick() {
		sleepTimer?.stop()
	}

	private fun onSetClicked(duration: Duration) {
		binding.timerDelay.setText(duration.inWholeMinutes.toString())
	}

	private fun onAddClicked(duration: Duration) {
		sleepTimer?.addTime(duration)
	}

	override fun onIsRunningChanged(isRunning: Boolean) {
		if (binding.isNotRunningGroup.isVisible || binding.isRunningGroup.isVisible) {
			TransitionManager.beginDelayedTransition(requireView().parent as ViewGroup)
		}

		sleepTimer?.let {
			binding.isNotRunningGroup.isVisible = !it.isRunning
			binding.isRunningGroup.isVisible = it.isRunning
		}

		if (isRunning) {
			tickTimer = fixedRateTimer(period = 250) {
				tick()
			}
		} else {
			tickTimer?.cancel()
		}
	}

	private fun tick() {
		sleepTimer?.let {
			binding.countdown.text = DateUtils.formatElapsedTime(it.timeLeft.inWholeSeconds)
		}
	}
}
