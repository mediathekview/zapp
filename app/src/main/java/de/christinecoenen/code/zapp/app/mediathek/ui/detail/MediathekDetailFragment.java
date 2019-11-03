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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;
import de.christinecoenen.code.zapp.utils.system.PermissionHelper;
import timber.log.Timber;


public class MediathekDetailFragment extends Fragment {

	private static final String ARG_SHOW = "ARG_SHOW";


	@BindView(R.id.text_show_topic)
	protected TextView topicView;

	@BindView(R.id.text_show_title)
	protected TextView titleView;

	@BindView(R.id.text_show_description)
	protected TextView descriptionView;

	@BindView(R.id.text_show_time)
	protected TextView timeView;

	@BindView(R.id.text_show_channel)
	protected TextView channelView;

	@BindView(R.id.text_show_duration)
	protected TextView durationView;

	@BindView(R.id.text_show_subtitle)
	protected TextView subtitleView;

	@BindView(R.id.quality_row_high)
	protected View qualityRowHigh;

	@BindView(R.id.quality_row_medium)
	protected View qualityRowMedium;

	@BindView(R.id.quality_row_low)
	protected View qualityRowLow;

	@BindView(R.id.quality_row_subtitle)
	protected View qualityRowSubtitle;

	@BindView(R.id.btn_download_high)
	protected ImageButton downloadButtonHigh;

	@BindView(R.id.btn_download_medium)
	protected ImageButton downloadButtonMedium;

	@BindView(R.id.btn_download_low)
	protected ImageButton downloadButtonLow;


	private MediathekShow show;
	private SettingsRepository settingsRepository;


	public MediathekDetailFragment() {
		// Required empty public constructor
	}

	public static MediathekDetailFragment getInstance(MediathekShow show) {
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
		View view = inflater.inflate(R.layout.fragment_mediathek_detail, container, false);
		ButterKnife.bind(this, view);

		topicView.setText(show.getTopic());
		titleView.setText(show.getTitle());
		descriptionView.setText(show.getDescription());

		timeView.setText(show.getFormattedTimestamp());
		channelView.setText(show.getChannel());
		durationView.setText(show.getFormattedDuration());
		subtitleView.setVisibility(show.hasSubtitle() ? View.VISIBLE : View.GONE);

		qualityRowHigh.setVisibility(show.hasStreamingQualityHd() ? View.VISIBLE : View.GONE);
		qualityRowMedium.setVisibility(show.hasStreamingQualityMedium() ? View.VISIBLE : View.GONE);
		qualityRowLow.setVisibility(show.hasStreamingQualityLow() ? View.VISIBLE : View.GONE);
		qualityRowSubtitle.setVisibility(show.hasSubtitle() ? View.VISIBLE : View.GONE);

		downloadButtonHigh.setEnabled(show.hasDownloadQualityHd());
		downloadButtonMedium.setEnabled(show.hasDownloadQualityMedium());
		downloadButtonLow.setEnabled(show.hasDownloadQualityLow());

		return view;
	}

	@OnClick(R.id.btn_play)
	protected void onPlayClick() {
		startActivity(MediathekPlayerActivity.getStartIntent(getContext(), show));
	}

	@OnClick(R.id.btn_website)
	protected void onWebsiteClick() {
		IntentHelper.openUrl(getContext(), show.getWebsiteUrl());
	}

	@OnClick(R.id.btn_download_high)
	protected void onDownloadHighClick() {
		download(show.getVideoUrlHd(), show.getDownloadFileNameHd());
	}

	@OnClick(R.id.btn_download_medium)
	protected void onDownloadMediumClick() {
		download(show.getVideoUrl(), show.getDownloadFileName());
	}

	@OnClick(R.id.btn_download_low)
	protected void onDownloadLowClick() {
		download(show.getVideoUrlLow(), show.getDownloadFileNameLow());
	}

	@OnClick(R.id.btn_download_subtitle)
	protected void onDownloadSubtitleClick() {
		download(show.getSubtitleUrl(), show.getDownloadFileNameSubtitle());
	}

	@OnClick(R.id.btn_share_high)
	protected void onShareHighClick() {
		share(show.getVideoUrlHd());
	}

	@OnClick(R.id.btn_share_medium)
	protected void onShareMediumClick() {
		share(show.getVideoUrl());
	}

	@OnClick(R.id.btn_share_low)
	protected void onShareLowClick() {
		share(show.getVideoUrlLow());
	}

	private void share(String url) {
		Intent videoIntent = new Intent(Intent.ACTION_VIEW);
		videoIntent.setDataAndType(Uri.parse(url), "video/*");
		startActivity(Intent.createChooser(videoIntent, getString(R.string.action_share)));
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
				.make(Objects.requireNonNull(getView()), R.string.error_mediathek_download_over_wifi_only, Snackbar.LENGTH_LONG);
			snackbar.setAction(R.string.activity_settings_title, v -> startActivity(SettingsActivity.getStartIntent(getContext())));
			snackbar.show();
		} else {
			long downloadId = downloadManager.enqueue(request);

			String infoString = getString(R.string.fragment_mediathek_download_started, show.getTitle());
			Snackbar snackbar = Snackbar
				.make(Objects.requireNonNull(getView()), infoString, Snackbar.LENGTH_LONG);
			snackbar.setAction(R.string.action_cancel, v -> downloadManager.remove(downloadId));
			snackbar.show();
		}
	}
}
