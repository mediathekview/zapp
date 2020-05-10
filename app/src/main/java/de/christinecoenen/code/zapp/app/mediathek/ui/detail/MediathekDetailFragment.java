package de.christinecoenen.code.zapp.app.mediathek.ui.detail;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Status;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.DownloadController;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.ISingleDownloadListener;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.DownloadException;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity;
import de.christinecoenen.code.zapp.databinding.FragmentMediathekDetailBinding;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;
import de.christinecoenen.code.zapp.utils.system.PermissionHelper;
import timber.log.Timber;


public class MediathekDetailFragment extends Fragment implements ISingleDownloadListener {

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

		binding.qualities.rowHigh.setVisibility(show.hasStreamingQuality(Quality.High) ? View.VISIBLE : View.GONE);
		binding.qualities.rowMedium.setVisibility(show.hasStreamingQuality(Quality.Medium) ? View.VISIBLE : View.GONE);
		binding.qualities.rowLow.setVisibility(show.hasStreamingQuality(Quality.Low) ? View.VISIBLE : View.GONE);

		binding.qualities.downloadButtonHigh.setEnabled(show.hasDownloadQuality(Quality.High));
		binding.qualities.downloadButtonMedium.setEnabled(show.hasDownloadQuality(Quality.Medium));
		binding.qualities.downloadButtonLow.setEnabled(show.hasDownloadQuality(Quality.Low));

		boolean isMissingDownloadsErrorVisible = !show.hasDownloadQuality(Quality.High) ||
			!show.hasDownloadQuality(Quality.Medium) ||
			!show.hasDownloadQuality(Quality.Low);
		binding.qualities.error.setVisibility(isMissingDownloadsErrorVisible ? View.VISIBLE : View.GONE);

		binding.play.setOnClickListener(this::onPlayClick);
		binding.buttons.website.setOnClickListener(this::onWebsiteClick);
		binding.buttons.download.setOnClickListener(this::onDownloadClick);

		binding.qualities.downloadButtonHigh.setOnClickListener(this::onDownloadHighClick);
		binding.qualities.downloadButtonMedium.setOnClickListener(this::onDownloadMediumClick);
		binding.qualities.downloadButtonLow.setOnClickListener(this::onDownloadLowClick);

		binding.qualities.shareButtonHigh.setOnClickListener(this::onShareHighClick);
		binding.qualities.shareButtonMedium.setOnClickListener(this::onShareMediumClick);
		binding.qualities.shareButtonLow.setOnClickListener(this::onShareLowClick);

		adjustDownloadButton(Status.NONE);
		downloadController.addSigleDownloadListener(show.getId(), this);

		return binding.getRoot();
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
		adjustDownloadButton(downloadStatus);
	}

	private void onPlayClick(View view) {
		startActivity(MediathekPlayerActivity.getStartIntent(getContext(), show));
	}

	private void onWebsiteClick(View view) {
		IntentHelper.openUrl(requireContext(), show.getWebsiteUrl());
	}

	private void onDownloadClick(View view) {
		switch (downloadStatus) {
			case NONE:
			case CANCELLED:
			case DELETED:
			case PAUSED:
			case REMOVED:
			case FAILED:
				// TODO: show dialog to choose quality
				download(Quality.Medium);
				break;
			case ADDED:
			case QUEUED:
			case DOWNLOADING:
				downloadController.stopDownload(show.getId());
				break;
			case COMPLETED:
				// TODO: show dialog
				downloadController.deleteDownload(show.getId());
				break;
		}
	}

	private void onDownloadHighClick(View view) {
		download(Quality.High);
	}

	private void onDownloadMediumClick(View view) {
		download(Quality.Medium);
	}

	private void onDownloadLowClick(View view) {
		download(Quality.Low);
	}

	private void onShareHighClick(View view) {
		share(Quality.High);
	}

	private void onShareMediumClick(View view) {
		share(Quality.Medium);
	}

	private void onShareLowClick(View view) {
		share(Quality.Low);
	}

	private void adjustDownloadButton(Status status) {
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
				// TODO: use colored style
				binding.buttons.downloadProgress.setVisibility(View.GONE);
				binding.buttons.download.setText(R.string.fragment_mediathek_download_delete);
				binding.buttons.download.setIconResource(R.drawable.ic_delete_white_24dp);
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

	private void download(Quality quality) {
		if (!PermissionHelper.writeExternalStorageAllowed(this)) {
			Timber.w("no permission to download show");
			return;
		}

		try {
			downloadController.startDownload(show, quality);
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
