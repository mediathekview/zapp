package de.christinecoenen.code.zapp.app.mediathek.ui.detail;


import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;
import de.christinecoenen.code.zapp.utils.system.PermissionHelper;


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

	@BindView(R.id.quality_row_low)
	protected View qualityRowLow;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mediathek_detail, container, false);
		ButterKnife.bind(this, view);

		topicView.setText(show.getTopic());
		titleView.setText(show.getTitle());
		descriptionView.setText(show.getDescription());

		timeView.setText(show.getFormattedTimestamp());
		channelView.setText(show.getChannel());
		durationView.setText(show.getFormattedDuration());
		subtitleView.setVisibility(show.hasSubtitle() ? View.VISIBLE : View.GONE);

		qualityRowHigh.setVisibility(show.hasQualityHd() ? View.VISIBLE : View.GONE);
		qualityRowLow.setVisibility(show.hasQualityLow() ? View.VISIBLE : View.GONE);
		qualityRowSubtitle.setVisibility(show.hasSubtitle() ? View.VISIBLE : View.GONE);

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

	private void download(String url, String downloadFileName) {
		if (!PermissionHelper.writeExternalStorageAllowed(this)) {
			return;
		}

		Uri uri = Uri.parse(url);

		// create request for android download manager
		DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Request request = new DownloadManager.Request(uri);

		// setting title and directory of request
		request.setTitle(show.getTitle());
		request.allowScanningByMediaScanner();
		request.setVisibleInDownloadsUi(true);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "zapp/" + downloadFileName);

		// enqueue download
		downloadManager.enqueue(request);
	}
}
