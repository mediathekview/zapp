package de.christinecoenen.code.zapp.app.player

import android.app.Dialog
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.christinecoenen.code.zapp.databinding.BottomSheetSleepTimerBinding
import java.time.Duration
import java.util.*
import kotlin.concurrent.fixedRateTimer

class SleepTimerBottomSheet : BottomSheetDialogFragment(), SleepTimer.Listener {

	private var _binding: BottomSheetSleepTimerBinding? = null
	private val binding: BottomSheetSleepTimerBinding get() = _binding!!

	private var tickTimer: Timer? = null
	private var sleepTimer: SleepTimer? = null

	private val backgroundPlayerServiceConnection: ServiceConnection = object : ServiceConnection {

		override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
			val binder = service as BackgroundPlayerService.Binder

			binder.getPlayer().sleepTimer.let {
				sleepTimer = it
				it.setListener(this@SleepTimerBottomSheet)
			}
		}

		override fun onServiceDisconnected(componentName: ComponentName) {
			sleepTimer?.setListener(null)
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
		dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
		return dialog
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = BottomSheetSleepTimerBinding.inflate(inflater, container, false)

		binding.startButton.setOnClickListener { onStartClick() }
		binding.stopButton.setOnClickListener { onStopClick() }

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		BackgroundPlayerService.bind(requireContext(), backgroundPlayerServiceConnection)
	}

	override fun onDestroyView() {
		requireContext().unbindService(backgroundPlayerServiceConnection)

		tickTimer?.cancel()
		_binding = null

		super.onDestroyView()
	}

	private fun onStartClick() {
		val minutes = binding.timerDelay.text.toString().toLongOrNull()

		if (minutes == null) {
			sleepTimer?.stop()
		} else {
			sleepTimer?.start(Duration.ofMinutes(minutes))
		}
	}

	private fun onStopClick() {
		sleepTimer?.stop()
	}

	override fun onIsRunningChanged(isRunning: Boolean) {
		sleepTimer?.let {
			binding.startButton.isVisible = !it.isRunning
			binding.timerDelayLayout.isVisible = !it.isRunning
			binding.countdown.isVisible = it.isRunning
			binding.stopButton.isVisible = it.isRunning
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
			binding.countdown.text = DateUtils.formatElapsedTime(it.timeLeft.seconds)
		}
	}
}
