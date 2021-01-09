package de.christinecoenen.code.zapp.app.settings.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.christinecoenen.code.zapp.R

class ChannelSelectionHelpDialog : DialogFragment() {

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(requireActivity())
			.setTitle(R.string.activity_channel_selection_title)
			.setMessage(R.string.activity_channel_selection_help_text)
			.setPositiveButton(android.R.string.ok, null)
			.create()
	}

}
