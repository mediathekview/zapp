package de.christinecoenen.code.zapp.app.mediathek.ui.detail;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import de.christinecoenen.code.zapp.R;


public class ConfirmFileDeletionDialog extends AppCompatDialogFragment {

	private Listener listener;

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		if (getTargetFragment() instanceof Listener) {
			listener = (Listener) getTargetFragment();
		} else {
			throw new IllegalArgumentException("Parent fragment must implement ConfirmFileDeletionDialog.Listener interface.");
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		return new AlertDialog.Builder(requireActivity())
			.setTitle(R.string.fragment_mediathek_confirm_delete_dialog_title)
			.setMessage(R.string.fragment_mediathek_confirm_delete_dialog_text)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> listener.onConfirmDeleteDialogOkClicked())
			.create();
	}

	interface Listener {
		void onConfirmDeleteDialogOkClicked();
	}
}
