package de.christinecoenen.code.zapp.app.mediathek.ui.detail;


import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekMedia;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;
import de.christinecoenen.code.zapp.utils.system.PermissionHelper;
import timber.log.Timber;


public class MediathekDetailFragment extends Fragment implements QualityRowView.Listener {

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

	@BindView(R.id.qualities_container)
	protected LinearLayout qualitiesContainer;

	@BindView(R.id.quality_row_subtitle)
	protected View qualityRowSubtitle;


	private MediathekShow show;


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

		qualityRowSubtitle.setVisibility(show.hasSubtitle() ? View.VISIBLE : View.GONE);

		for (MediathekMedia media : show.getMedia()) {
			if (media.isSubtitle()) {
				continue;
			}

			QualityRowView qualityRowView = new QualityRowView(getContext());
			qualityRowView.setMedia(media);
			qualityRowView.setListener(this);
			qualitiesContainer.addView(qualityRowView, 1);
		}

		return view;
	}

	@OnClick(R.id.btn_play)
	protected void onPlayClick() {
		startActivity(MediathekPlayerActivity.getStartIntent(getContext(), show));
	}

	@OnClick(R.id.btn_website)
	protected void onWebsiteClick() {
		IntentHelper.openUrl(getContext(), show.getWebsite());
	}

	@OnClick(R.id.btn_download_subtitle)
	protected void onDownloadSubtitleClick() {
		download(show.getSubtitleUrl(), show.getDownloadFileNameSubtitle());
	}

	@Override
	public void onDownloadClick(MediathekMedia media) {
		download(media.getUrl(), show.getDownloadFileName(media));
	}

	@Override
	public void onShareClick(MediathekMedia media) {
		share(media.getUrl());
	}

	private void share(String url) {
		Intent videoIntent = new Intent(Intent.ACTION_VIEW);
		videoIntent.setDataAndType(Uri.parse(url), "video/*");
		startActivity(videoIntent);
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
		} catch (Exception e) {
			request = null;
		} finally {
			if (request == null) {
				Toast.makeText(getContext(), R.string.error_mediathek_invalid_url, Toast.LENGTH_LONG).show();
			} else {
				enqueDownload(request, downloadFileName);
			}
		}
	}

	private void enqueDownload(DownloadManager.Request request, String downloadFileName) {
		// setting title and directory of request
		request.setTitle(show.getTitle());
		request.allowScanningByMediaScanner();
		request.setVisibleInDownloadsUi(true);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "zapp/" + downloadFileName);

		// enqueue download
		DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		if (downloadManager == null) {
			Toast.makeText(getContext(), R.string.error_mediathek_no_download_manager, Toast.LENGTH_LONG).show();
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
