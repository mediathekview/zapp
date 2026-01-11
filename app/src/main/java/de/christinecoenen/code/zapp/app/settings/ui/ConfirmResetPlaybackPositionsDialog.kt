package de.christinecoenen.code.zapp.app.settings.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.christinecoenen.code.zapp.R

class ConfirmResetPlaybackPositionsDialog : AppCompatDialogFragment() {

	companion object {
		const val REQUEST_KEY_CONFIRMED = "REQUEST_KEY_CONFIRMED"
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return MaterialAlertDialogBuilder(requireActivity())
			.setTitle(R.string.pref_dialog_confirm_reset_playback_position_title)
			.setMessage(R.string.pref_dialog_confirm_reset_playback_position_text)
			.setIcon(R.drawable.ic_outline_play_circle_24)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok) { _, _ ->
				setFragmentResult(REQUEST_KEY_CONFIRMED, Bundle())
			}
			.create()
	}
}
