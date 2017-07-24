package de.christinecoenen.code.zapp.app.mediathek.ui.detail;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

		return view;
	}

	@OnClick(R.id.btn_play)
	protected void onPlayClick() {
		startActivity(MediathekPlayerActivity.getStartIntent(getContext(), show));
	}

	@OnClick(R.id.btn_website)
	protected void onWebsiteClick() {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(show.getWebsiteUrl()));
		startActivity(browserIntent);
	}
}
