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

public class WatchStreamActivity extends FullscreenActivity {

	private static final String STREAM_URL = "http://kika_geo-lh.akamaihd.net/i/livetvkika_de@75114/master.m3u8";

	@BindView(R.id.video) VideoView videoView;
	@BindView(R.id.play_pause_button) FloatingActionButton playPauseButton;

	@BindDrawable(android.R.drawable.ic_media_pause) Drawable pauseIcon;
	@BindDrawable(android.R.drawable.ic_media_play) Drawable playIcon;

	protected boolean isPlaying = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		videoView.setVideoPath(STREAM_URL);
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
				videoIntent.setDataAndType(Uri.parse(STREAM_URL), "video/*");
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
