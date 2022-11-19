package de.christinecoenen.code.zapp.app.mediathek.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.christinecoenen.code.zapp.R

class ConfirmRemoveDownloadDialog : AppCompatDialogFragment() {

	companion object {
		const val REQUEST_KEY_CONFIRMED = "REQUEST_KEY_CONFIRMED"
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return MaterialAlertDialogBuilder(requireActivity())
			.setTitle(R.string.fragment_downloads_confirm_show_removal_dialog_title)
			.setMessage(R.string.fragment_downloads_confirm_show_removal_dialog_text)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok) { _, _ ->
				setFragmentResult(REQUEST_KEY_CONFIRMED, Bundle())
			}
			.create()
	}
}
