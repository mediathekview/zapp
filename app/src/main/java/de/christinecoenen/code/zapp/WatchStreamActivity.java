package de.christinecoenen.code.zapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.VideoView;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.model.XmlResourcesChannelList;

public class WatchStreamActivity extends FullscreenActivity {

	public static final String EXTRA_CHANNEL_ID = "extra_channel_id";

	@BindView(R.id.video) VideoView videoView;
	@BindView(R.id.play_pause_button) FloatingActionButton playPauseButton;

	@BindDrawable(android.R.drawable.ic_media_pause) Drawable pauseIcon;
	@BindDrawable(android.R.drawable.ic_media_play) Drawable playIcon;

	protected ChannelModel currentChannel;
	protected IChannelList channelList;
	protected boolean isPlaying = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this);

		channelList = new XmlResourcesChannelList(this);

		Bundle extras = getIntent().getExtras();
		int channelId = extras.getInt(EXTRA_CHANNEL_ID);
		currentChannel = channelList.get(channelId);
		setTitle(currentChannel.getName());
	}

	@Override
	protected void onStart() {
		super.onStart();

		videoView.setVideoPath(currentChannel.getStreamUrl());
		play();
	}

	@Override
	protected void onPause() {
		super.onPause();
		videoView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (isPlaying) {
			play();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				Intent videoIntent = new Intent(Intent.ACTION_VIEW);
				videoIntent.setDataAndType(Uri.parse(currentChannel.getStreamUrl()), "video/*");
				startActivity(videoIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected int getViewId() {
		return R.layout.activity_watch_stream;
	}

	@SuppressWarnings("unused")
	@OnClick(R.id.play_pause_button)
	public void OnPlayPauseClick() {
		if (isPlaying) {
			pause();
		} else {
			play();
		}
	}

	@SuppressWarnings("unused")
	@OnTouch(R.id.play_pause_button)
	public boolean OnPlayPauseTouch() {
		delayHide();
		return false;
	}

	protected void play() {
		isPlaying = true;
		videoView.start();
		playPauseButton.setImageDrawable(pauseIcon);
	}

	protected void pause() {
		isPlaying = false;
		videoView.pause();
		playPauseButton.setImageDrawable(playIcon);
	}
}
