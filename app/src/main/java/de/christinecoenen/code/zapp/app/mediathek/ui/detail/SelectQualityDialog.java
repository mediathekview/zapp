package de.christinecoenen.code.zapp.app.mediathek.ui.detail;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.List;
import java.util.Objects;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;


public class SelectQualityDialog extends AppCompatDialogFragment {

	private static final String ARGUMENT_MEDIATHEK_SHOW = "ARGUMENT_MEDIATHEK_SHOW";

	static SelectQualityDialog newInstance(MediathekShow mediathekShow) {
		SelectQualityDialog fragment = new SelectQualityDialog();

		Bundle args = new Bundle();
		args.putSerializable(ARGUMENT_MEDIATHEK_SHOW, mediathekShow);
		fragment.setArguments(args);

		return fragment;
	}

	private String[] qualities;
	private Listener listener;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		assert getArguments() != null;

		MediathekShow show = (MediathekShow) getArguments().getSerializable(ARGUMENT_MEDIATHEK_SHOW);
		List<Quality> supportedQualities = Objects.requireNonNull(show).getSupportedDownloadQualities();

		qualities = new String[supportedQualities.size()];

		for (int i = 0; i < supportedQualities.size(); i++) {
			qualities[i] = getString(supportedQualities.get(i).getLabelResId());
		}
	}

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
			.setTitle(R.string.fragment_mediathek_qualities_title)
			.setItems(qualities, (dialogInterface, i) -> listener.onQualitySelected(Quality.values()[i]))
			.setNegativeButton(android.R.string.cancel, null)
			.create();
	}

	interface Listener {
		void onQualitySelected(Quality quality);
	}
}
