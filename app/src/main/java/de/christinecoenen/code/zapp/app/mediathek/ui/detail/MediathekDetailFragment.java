package de.christinecoenen.code.zapp.app.mediathek.ui.detail;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Status;

import java.io.IOException;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.DownloadController;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.ISingleDownloadListener;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.DownloadException;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity;
import de.christinecoenen.code.zapp.databinding.FragmentMediathekDetailBinding;
import de.christinecoenen.code.zapp.utils.system.ImageHelper;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;


public class MediathekDetailFragment extends Fragment implements ISingleDownloadListener,
	ConfirmFileDeletionDialog.Listener,
	SelectQualityDialog.Listener {

	private static final String ARG_SHOW = "ARG_SHOW";


	private FragmentMediathekDetailBinding binding;
	private MediathekShow show;
	private DownloadController downloadController;
	private Status downloadStatus = Status.NONE;


	public MediathekDetailFragment() {
		// Required empty public constructor
	}

	static MediathekDetailFragment getInstance(MediathekShow show) {
		MediathekDetailFragment fragment = new MediathekDetailFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARG_SHOW, show);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		downloadController = new DownloadController(context);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		downloadController = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			show = (MediathekShow) getArguments().getSerializable(ARG_SHOW);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentMediathekDetailBinding.inflate(inflater, container, false);

		binding.texts.topic.setText(show.getTopic());
		binding.texts.title.setText(show.getTitle());
		binding.texts.description.setText(show.getDescription());

		binding.time.setText(show.getFormattedTimestamp());
		binding.channel.setText(show.getChannel());
		binding.duration.setText(show.getFormattedDuration());
		binding.subtitle.setVisibility(show.hasSubtitle() ? View.VISIBLE : View.GONE);

		binding.buttons.download.setEnabled(show.hasAnyDownloadQuality());

		binding.play.setOnClickListener(this::onPlayClick);
		binding.buttons.download.setOnClickListener(this::onDownloadClick);
		binding.buttons.share.setOnClickListener(this::onShareClick);
		binding.buttons.website.setOnClickListener(this::onWebsiteClick);

		adjustUiToDownloadStatus(Status.NONE, null);
		downloadController.addSigleDownloadListener(show.getId(), this);

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		downloadController.deleteDownloadsWithDeletedFiles();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		downloadController.removeSigleDownloadListener(this);
	}

	@Override
	public void onDownloadProgressChanged(@NonNull Download download) {
		binding.buttons.downloadProgress.setProgress(download.getProgress());
	}

	@Override
	public void onDownloadStatusChanged(@NonNull Download download) {
		downloadStatus = download.getStatus();
		adjustUiToDownloadStatus(downloadStatus, download);
	}

	@Override
	public void onConfirmDeleteDialogOkClicked() {
		downloadController.deleteDownload(show.getId());
	}

	@Override
	public void onDownloadQualitySelected(Quality quality) {
		download(quality);
	}

	@Override
	public void onShareQualitySelected(Quality quality) {
		share(quality);
	}

	private void onPlayClick(View view) {
		startActivity(MediathekPlayerActivity.getStartIntent(getContext(), show));
	}

	private void onDownloadClick(View view) {
		switch (downloadStatus) {
			case NONE:
			case CANCELLED:
			case DELETED:
			case PAUSED:
			case REMOVED:
			case FAILED:
				showSelectQualityDialog(SelectQualityDialog.Mode.DOWNLOAD);
				break;
			case ADDED:
			case QUEUED:
			case DOWNLOADING:
				downloadController.stopDownload(show.getId());
				break;
			case COMPLETED:
				showConfirmDeleteDialog();
				break;
		}
	}

	private void onShareClick(View view) {
		showSelectQualityDialog(SelectQualityDialog.Mode.SHARE);
	}

	private void onWebsiteClick(View view) {
		IntentHelper.openUrl(requireContext(), show.getWebsiteUrl());
	}

	private void showConfirmDeleteDialog() {
		DialogFragment newFragment = new ConfirmFileDeletionDialog();
		newFragment.setTargetFragment(this, 0);
		newFragment.show(getParentFragmentManager(), null);
	}

	private void showSelectQualityDialog(SelectQualityDialog.Mode mode) {
		DialogFragment newFragment = SelectQualityDialog.newInstance(show, mode);
		newFragment.setTargetFragment(this, 0);
		newFragment.show(getParentFragmentManager(), null);
	}

	private void adjustUiToDownloadStatus(Status status, Download download) {
		binding.texts.thumbnail.setVisibility(View.GONE);

		switch (status) {
			case NONE:
			case CANCELLED:
			case DELETED:
			case PAUSED:
			case REMOVED:
				binding.buttons.downloadProgress.setVisibility(View.GONE);
				binding.buttons.download.setText(R.string.fragment_mediathek_download);
				binding.buttons.download.setIconResource(R.drawable.ic_file_download_white_24dp);
				break;
			case ADDED:
			case QUEUED:
				binding.buttons.downloadProgress.setVisibility(View.VISIBLE);
				binding.buttons.downloadProgress.setIndeterminate(true);
				binding.buttons.download.setText(R.string.fragment_mediathek_download_running);
				binding.buttons.download.setIconResource(R.drawable.ic_stop_white_24dp);
				break;
			case DOWNLOADING:
				binding.buttons.downloadProgress.setVisibility(View.VISIBLE);
				binding.buttons.downloadProgress.setIndeterminate(false);
				binding.buttons.download.setText(R.string.fragment_mediathek_download_running);
				binding.buttons.download.setIconResource(R.drawable.ic_stop_white_24dp);
				break;
			case COMPLETED:
				binding.buttons.downloadProgress.setVisibility(View.GONE);
				binding.buttons.download.setText(R.string.fragment_mediathek_download_delete);
				binding.buttons.download.setIconResource(R.drawable.ic_delete_white_24dp);

				Bitmap thumbnail;
				try {
					thumbnail = ImageHelper.loadThumbnail(getContext(), download.getFile());
					binding.texts.thumbnail.setImageBitmap(thumbnail);
					binding.texts.thumbnail.setVisibility(View.VISIBLE);
				} catch (IOException ignored) {
				}

				break;
			case FAILED:
				binding.buttons.downloadProgress.setVisibility(View.GONE);
				binding.buttons.download.setText(R.string.fragment_mediathek_download_retry);
				binding.buttons.download.setIconResource(R.drawable.ic_warning_white_24dp);
				break;
		}
	}

	private void share(Quality quality) {
		Intent videoIntent = new Intent(Intent.ACTION_VIEW);
		String url = show.getVideoUrl(quality);
		videoIntent.setDataAndType(Uri.parse(url), "video/*");
		startActivity(Intent.createChooser(videoIntent, getString(R.string.action_share)));
	}

	private void download(Quality downloadQuality) {
		try {
			downloadController.startDownload(show, downloadQuality);
		} catch (WrongNetworkConditionException e) {
			Snackbar snackbar = Snackbar
				.make(requireView(), R.string.error_mediathek_download_over_wifi_only, Snackbar.LENGTH_LONG);
			snackbar.setAction(R.string.activity_settings_title, v -> startActivity(SettingsActivity.getStartIntent(getContext())));
			snackbar.show();
		} catch (DownloadException e) {
			Toast.makeText(getContext(), R.string.error_mediathek_no_download_manager, Toast.LENGTH_LONG).show();
		}
	}
}
