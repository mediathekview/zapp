package de.christinecoenen.code.zapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.VideoView;

public class WatchStreamActivity extends FullscreenActivity {

	private static final String STREAM_URL = "http://kika_geo-lh.akamaihd.net/i/livetvkika_de@75114/master.m3u8";

	protected VideoView videoView;
	protected FloatingActionButton playPauseButton;

	protected boolean isPlaying = false;

	private final View.OnClickListener mPlayPauseClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (isPlaying) {
				pause();
			} else {
				play();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		videoView = (VideoView) findViewById(R.id.video);
		playPauseButton = (FloatingActionButton) findViewById(R.id.play_pause_button);

		playPauseButton.setOnTouchListener(mDelayHideTouchListener);
		playPauseButton.setOnClickListener(mPlayPauseClickListener);
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
				Intent videoIntent =new Intent(Intent.ACTION_VIEW);
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

	protected void play() {
		isPlaying = true;
		videoView.start();

		Drawable playIcon = ContextCompat.
				getDrawable(getApplicationContext(), android.R.drawable.ic_media_play);
		playPauseButton.setImageDrawable(playIcon);
	}

	protected void pause() {
		isPlaying = false;
		videoView.pause();

		Drawable pauseIcon = ContextCompat.
				getDrawable(getApplicationContext(), android.R.drawable.ic_media_pause);
		playPauseButton.setImageDrawable(pauseIcon);
	}
}
