package de.christinecoenen.code.zapp.app.player

import android.app.Dialog
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.DialogSleepTimerBinding

class SleepTimerDialog(
	private val onTimerChanged: (Long?) -> Unit
) : DialogFragment() {
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val binding = DialogSleepTimerBinding.inflate(layoutInflater)
		val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		val lastSleepTimerDelay = preferences.getLong(getString(R.string.pref_key_sleep_timer_delay), 30)
		binding.timerDelay.setText(lastSleepTimerDelay.toString())

		return MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.sleep_timer)
			.setView(binding.root)
			.setPositiveButton(android.R.string.ok) { _, _ ->
				val delay = binding.timerDelay.text.toString().toLongOrNull()
				// save the selected timer delay to be shown again in the next session
				delay?.let {
					preferences.edit(true) {
						putLong(getString(R.string.pref_key_sleep_timer_delay), it)
					}
				}
				// multiply with 60: from minutes to seconds
				onTimerChanged(delay?.times(60))
			}
			.setNeutralButton(R.string.action_reset) { _, _ ->
				onTimerChanged.invoke(null)
			}
			.setNegativeButton(R.string.action_cancel, null)
			.create()
	}
}
