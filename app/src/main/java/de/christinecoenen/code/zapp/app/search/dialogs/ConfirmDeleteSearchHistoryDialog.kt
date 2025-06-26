package de.christinecoenen.code.zapp.app.search.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.christinecoenen.code.zapp.R

class ConfirmDeleteSearchHistoryDialog : AppCompatDialogFragment() {

	companion object {
		const val REQUEST_KEY_CONFIRMED = "REQUEST_KEY_CONFIRMED"
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return MaterialAlertDialogBuilder(requireActivity())
			.setTitle(R.string.search_dialog_confirm_delete_search_history_title)
			.setMessage(R.string.search_dialog_confirm_delete_search_history_text)
			.setIcon(R.drawable.ic_history_24)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok) { _, _ ->
				setFragmentResult(REQUEST_KEY_CONFIRMED, Bundle())
			}
			.create()
	}
}
