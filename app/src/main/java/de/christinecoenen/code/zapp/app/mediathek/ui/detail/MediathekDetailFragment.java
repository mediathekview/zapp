package de.christinecoenen.code.zapp.app.mediathek.ui.detail;


import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity;
import de.christinecoenen.code.zapp.databinding.FragmentMediathekDetailBinding;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;
import de.christinecoenen.code.zapp.utils.system.PermissionHelper;
import timber.log.Timber;


public class MediathekDetailFragment extends Fragment {

	private static final String ARG_SHOW = "ARG_SHOW";


	private MediathekShow show;
	private SettingsRepository settingsRepository;


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
		settingsRepository = new SettingsRepository(context);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		settingsRepository = null;
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
		FragmentMediathekDetailBinding binding = FragmentMediathekDetailBinding.inflate(inflater, container, false);

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
		binding.qualities.rowSubtitle.setVisibility(show.hasSubtitle() ? View.VISIBLE : View.GONE);

		binding.qualities.downloadButtonHigh.setEnabled(show.hasDownloadQuality(Quality.High));
		binding.qualities.downloadButtonMedium.setEnabled(show.hasDownloadQuality(Quality.Medium));
		binding.qualities.downloadButtonLow.setEnabled(show.hasDownloadQuality(Quality.Low));

		boolean isMissingDownloadsErrorVisible = !show.hasDownloadQuality(Quality.High) ||
			!show.hasDownloadQuality(Quality.Medium) ||
			!show.hasDownloadQuality(Quality.Low);
		binding.qualities.error.setVisibility(isMissingDownloadsErrorVisible ? View.VISIBLE : View.GONE);

		binding.play.setOnClickListener(this::onPlayClick);
		binding.buttons.website.setOnClickListener(this::onWebsiteClick);

		binding.qualities.downloadButtonHigh.setOnClickListener(this::onDownloadHighClick);
		binding.qualities.downloadButtonMedium.setOnClickListener(this::onDownloadMediumClick);
		binding.qualities.downloadButtonLow.setOnClickListener(this::onDownloadLowClick);
		binding.qualities.downloadButtonSubtitle.setOnClickListener(this::onDownloadSubtitleClick);

		binding.qualities.shareButtonHigh.setOnClickListener(this::onShareHighClick);
		binding.qualities.shareButtonMedium.setOnClickListener(this::onShareMediumClick);
		binding.qualities.shareButtonLow.setOnClickListener(this::onShareLowClick);

		return binding.getRoot();
	}

	private void onPlayClick(View view) {
		startActivity(MediathekPlayerActivity.getStartIntent(getContext(), show));
	}

	private void onWebsiteClick(View view) {
		IntentHelper.openUrl(requireContext(), show.getWebsiteUrl());
	}

	private void onDownloadHighClick(View view) {
		dowloadQuality(Quality.High);
	}

	private void onDownloadMediumClick(View view) {
		dowloadQuality(Quality.Medium);
	}

	private void onDownloadLowClick(View view) {
		dowloadQuality(Quality.Low);
	}

	private void onDownloadSubtitleClick(View view) {
		download(show.getSubtitleUrl(), show.getDownloadFileNameSubtitle());
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

	private void share(Quality quality) {
		Intent videoIntent = new Intent(Intent.ACTION_VIEW);
		String url = show.getVideoUrl(quality);
		videoIntent.setDataAndType(Uri.parse(url), "video/*");
		startActivity(Intent.createChooser(videoIntent, getString(R.string.action_share)));
	}

	private void dowloadQuality(Quality quality) {
		download(show.getVideoUrl(quality), show.getDownloadFileName(quality));
	}

	private void download(String url, String downloadFileName) {
		if (!PermissionHelper.writeExternalStorageAllowed(this)) {
			Timber.w("no permission to download show");
			return;
		}

		Uri uri = Uri.parse(url);

		DownloadManager.Request request = null;
		try {
			// create request for android download manager
			request = new DownloadManager.Request(uri);
		} finally {
			if (request == null) {
				Toast.makeText(getContext(), R.string.error_mediathek_invalid_url, Toast.LENGTH_LONG).show();
			} else {
				enqueueDownload(request, downloadFileName);
			}
		}
	}

	private void enqueueDownload(DownloadManager.Request request, String downloadFileName) {
		// setting title and directory of request
		request.setTitle(show.getTitle());
		request.allowScanningByMediaScanner();
		request.setVisibleInDownloadsUi(true);
		request.setAllowedOverMetered(!settingsRepository.getDownloadOverWifiOnly());
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "zapp/" + downloadFileName);

		// enqueue download
		DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

		if (downloadManager == null || connectivityManager == null) {
			Toast.makeText(getContext(), R.string.error_mediathek_no_download_manager, Toast.LENGTH_LONG).show();
		} else if (settingsRepository.getDownloadOverWifiOnly() && connectivityManager.isActiveNetworkMetered()) {
			Snackbar snackbar = Snackbar
				.make(requireView(), R.string.error_mediathek_download_over_wifi_only, Snackbar.LENGTH_LONG);
			snackbar.setAction(R.string.activity_settings_title, v -> startActivity(SettingsActivity.getStartIntent(getContext())));
			snackbar.show();
		} else {
			long downloadId = downloadManager.enqueue(request);

			String infoString = getString(R.string.fragment_mediathek_download_started, show.getTitle());
			Snackbar snackbar = Snackbar
				.make(requireView(), infoString, Snackbar.LENGTH_LONG);
			snackbar.setAction(R.string.action_cancel, v -> downloadManager.remove(downloadId));
			snackbar.show();
		}
	}
}
