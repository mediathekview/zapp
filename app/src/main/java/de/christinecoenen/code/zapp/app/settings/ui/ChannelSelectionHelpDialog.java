package de.christinecoenen.code.zapp.app.settings.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import de.christinecoenen.code.zapp.R;


@SuppressWarnings({"WeakerAccess", "RedundantSuppression"})
public class ChannelSelectionHelpDialog extends DialogFragment {

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		return new AlertDialog.Builder(requireActivity())
			.setTitle(R.string.activity_channel_selection_title)
			.setMessage(R.string.activity_channel_selection_help_text)
			.setPositiveButton(android.R.string.ok, null)
			.create();
	}

}
