package de.christinecoenen.code.zapp.app.player

import android.app.Dialog
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.DialogSleepTimerBinding
import java.time.Duration

class SleepTimerDialog : DialogFragment() {

	private lateinit var sleepTimer: SleepTimer
	private lateinit var binding: DialogSleepTimerBinding

	private val backgroundPlayerServiceConnection: ServiceConnection = object : ServiceConnection {

		override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
			val binder = service as BackgroundPlayerService.Binder
			sleepTimer = binder.getPlayer().sleepTimer
		}

		override fun onServiceDisconnected(componentName: ComponentName) {
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		binding = DialogSleepTimerBinding.inflate(layoutInflater)

		return MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.sleep_timer)
			.setView(binding.root)
			.setNegativeButton(R.string.action_cancel, null)
			.setPositiveButton(android.R.string.ok) { _, _ -> onOkClick() }
			.create()
	}

	override fun onStart() {
		super.onStart()
		BackgroundPlayerService.bind(requireContext(), backgroundPlayerServiceConnection)
	}

	override fun onStop() {
		super.onStop()
		requireContext().unbindService(backgroundPlayerServiceConnection)
	}

	private fun onOkClick() {
		val minutes = binding.timerDelay.text.toString().toLongOrNull()

		if (minutes == null) {
			sleepTimer.stop()
		} else {
			sleepTimer.start(Duration.ofMinutes(minutes))
		}
	}
}
