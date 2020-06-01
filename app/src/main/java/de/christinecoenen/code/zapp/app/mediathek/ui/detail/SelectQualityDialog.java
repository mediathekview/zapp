package de.christinecoenen.code.zapp.app.mediathek.ui.detail;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.List;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;


public class SelectQualityDialog extends AppCompatDialogFragment {

	private static final String ARGUMENT_MEDIATHEK_SHOW = "ARGUMENT_MEDIATHEK_SHOW";
	private static final String ARGUMENT_MODE = "ARGUMENT_MODE";

	static SelectQualityDialog newInstance(MediathekShow mediathekShow, Mode mode) {
		SelectQualityDialog fragment = new SelectQualityDialog();

		Bundle args = new Bundle();
		args.putSerializable(ARGUMENT_MEDIATHEK_SHOW, mediathekShow);
		args.putSerializable(ARGUMENT_MODE, mode);
		fragment.setArguments(args);

		return fragment;
	}

	private List<Quality> qualities;
	private String[] qualityLabels;
	private Mode mode;
	private Listener listener;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		assert getArguments() != null;

		mode = (Mode) getArguments().getSerializable(ARGUMENT_MODE);

		MediathekShow show = (MediathekShow) getArguments().getSerializable(ARGUMENT_MEDIATHEK_SHOW);
		assert show != null;

		switch (mode) {
			case DOWNLOAD:
				qualities = show.getSupportedDownloadQualities();
				break;
			case SHARE:
				qualities = show.getSupportedStreamingQualities();
				break;
		}

		qualityLabels = new String[qualities.size()];

		for (int i = 0; i < qualities.size(); i++) {
			qualityLabels[i] = getString(qualities.get(i).getLabelResId());
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
			.setItems(qualityLabels, this::onItemSelected)
			.setNegativeButton(android.R.string.cancel, null)
			.create();
	}

	private void onItemSelected(DialogInterface dialogInterface, int i) {
		Quality quality = qualities.get(i);

		switch (mode) {
			case DOWNLOAD:
				listener.onDownloadQualitySelected(quality);
				break;
			case SHARE:
				listener.onShareQualitySelected(quality);
				break;
		}
	}

	public enum Mode {
		DOWNLOAD,
		SHARE
	}

	interface Listener {
		void onDownloadQualitySelected(Quality quality);

		void onShareQualitySelected(Quality quality);
	}
}
