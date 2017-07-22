package de.christinecoenen.code.zapp.mediathek.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.MediathekShow;

public class MediathekPlayerActivity extends AppCompatActivity implements PlaybackControlView.VisibilityListener {

	private static final String EXTRA_SHOW = "de.christinecoenen.code.zapp.EXTRA_SHOW";
	private static final String ARG_VIDEO_MILLIS = "ARG_VIDEO_MILLIS";

	public static Intent getStartIntent(Context context, MediathekShow show) {
		Intent intent = new Intent(context, MediathekPlayerActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.putExtra(EXTRA_SHOW, show);
		return intent;
	}


	@BindView(R.id.fullscreen_content)
	protected View fullscreenContent;

	@BindView(R.id.toolbar)
	protected Toolbar toolbar;

	@BindView(R.id.video)
	protected SimpleExoPlayerView videoView;


	private SimpleExoPlayer player;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_mediathek_player);
		ButterKnife.bind(this);

		// set to show
		MediathekShow show = (MediathekShow) getIntent().getExtras().getSerializable(EXTRA_SHOW);
		if (show == null) {
			// TODO: handle error
			return;
		}

		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			setTitle(show.getTopic());
			getSupportActionBar().setSubtitle(show.getTitle());
		}

		// player
		DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
		DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
			Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter);
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
		TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
		player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
		// TODO: addShows error handling
		// TODO: addShows loading indicator

		videoView.setControllerVisibilityListener(this);
		videoView.setPlayer(player);
		videoView.requestFocus();

		Uri videoUri = Uri.parse(show.getVideoUrl());
		MediaSource videoSource = new ExtractorMediaSource(videoUri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);
		player.prepare(videoSource);
		player.setPlayWhenReady(true);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ARG_VIDEO_MILLIS, player.getCurrentPosition());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		long millis = savedInstanceState.getLong(ARG_VIDEO_MILLIS);
		player.seekTo(millis);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// TODO: addShows multiwindow support (see ChannelDetailActivity)
		// TODO: addShows lock screen orientation support (see ChannelDetailActivity)

		player.stop();
	}

	@Override
	public void onVisibilityChange(int visibility) {
		if (visibility == View.VISIBLE) {
			showSystemUi();
		} else {
			hideSystemUi();
		}
	}

	private void showSystemUi() {
		if (getSupportActionBar() != null) {
			getSupportActionBar().show();
		}
		fullscreenContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
	}

	private void hideSystemUi() {
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}

		fullscreenContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
			| View.SYSTEM_UI_FLAG_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}
}
