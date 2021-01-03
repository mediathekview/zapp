package de.christinecoenen.code.zapp.app.mediathek.ui.detail

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import de.christinecoenen.code.zapp.R

class ConfirmFileDeletionDialog : AppCompatDialogFragment() {

	private lateinit var listener: Listener

	override fun onAttach(context: Context) {
		super.onAttach(context)

		listener = if (targetFragment is Listener) {
			targetFragment as Listener
		} else {
			throw IllegalArgumentException("Parent fragment must implement ConfirmFileDeletionDialog.Listener interface.")
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(requireActivity())
			.setTitle(R.string.fragment_mediathek_confirm_delete_dialog_title)
			.setMessage(R.string.fragment_mediathek_confirm_delete_dialog_text)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok) { _, _ -> listener.onConfirmDeleteDialogOkClicked() }
			.create()
	}

	internal interface Listener {
		fun onConfirmDeleteDialogOkClicked()
	}
}
