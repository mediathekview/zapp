package de.christinecoenen.code.zapp.app.mediathek.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper;
import de.christinecoenen.code.zapp.utils.video.VideoBufferingHandler;
import de.christinecoenen.code.zapp.utils.video.VideoErrorHandler;

public class MediathekPlayerActivity extends AppCompatActivity implements
	PlaybackControlView.VisibilityListener,
	VideoErrorHandler.IVideoErrorListener,
	VideoBufferingHandler.IVideoBufferingListener {

	private static final String TAG = MediathekPlayerActivity.class.toString();
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

	@BindView(R.id.text_error)
	protected TextView errorView;

	@BindView(R.id.progress)
	protected ProgressBar loadingIndicator;

	private MediathekShow show;
	private SimpleExoPlayer player;
	private PlaybackControlView controlView;
	private MediaSource videoSource;
	private VideoErrorHandler videoErrorHandler;
	private VideoBufferingHandler bufferingHandler;
	private long millis = 0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_mediathek_player);
		ButterKnife.bind(this);

		// set to show
		show = (MediathekShow) getIntent().getExtras().getSerializable(EXTRA_SHOW);
		if (show == null) {
			Toast.makeText(this, R.string.error_mediathek_called_without_show, Toast.LENGTH_LONG).show();
			finish();
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

		videoErrorHandler = new VideoErrorHandler(this);
		player.addListener(videoErrorHandler);
		bufferingHandler = new VideoBufferingHandler(this);
		player.addListener(bufferingHandler);

		videoView.setControllerVisibilityListener(this);
		videoView.setPlayer(player);
		videoView.requestFocus();
		controlView = (PlaybackControlView) videoView.getChildAt(1);

		Uri videoUri = Uri.parse(show.getVideoUrl());
		videoSource = new ExtractorMediaSource(videoUri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ARG_VIDEO_MILLIS, player.getCurrentPosition());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		millis = savedInstanceState.getLong(ARG_VIDEO_MILLIS);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (MultiWindowHelper.isInsideMultiWindow(this)) {
			resumeActivity();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!MultiWindowHelper.isInsideMultiWindow(this)) {
			resumeActivity();
		}

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean lockScreen = preferences.getBoolean("pref_detail_landscape", true);
		if (lockScreen) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!MultiWindowHelper.isInsideMultiWindow(this)) {
			pauseActivity();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (MultiWindowHelper.isInsideMultiWindow(this)) {
			pauseActivity();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		player.removeListener(videoErrorHandler);
		player.removeListener(bufferingHandler);
		player.release();
	}

	@Override
	public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode);
		if (isInPictureInPictureMode) {
			controlView.hide();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_mediathek_player, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_share:
				IntentHelper.openUrl(this, show.getVideoUrl());
				return true;
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD:
			case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				rewind();
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_MEDIA_STEP_FORWARD:
			case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				fastForward();
				return true;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_MEDIA_TOP_MENU:
				toggleSystemUi();
				return true;
			case KeyEvent.KEYCODE_MEDIA_PLAY:
				resumeActivity();
				return true;
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				pauseActivity();
				return true;
			case KeyEvent.KEYCODE_MEDIA_CLOSE:
				finish();
				return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onVisibilityChange(int visibility) {
		if (visibility == View.VISIBLE) {
			showSystemUi();
		} else {
			hideSystemUi();
		}
	}

	@Override
	public void onVideoError(int messageResourceId) {
		showError(messageResourceId);
	}

	@Override
	public void onBufferingStarted() {
		loadingIndicator.setVisibility(View.VISIBLE);
	}

	@Override
	public void onBufferingEnded() {
		loadingIndicator.setVisibility(View.INVISIBLE);
	}

	private void pauseActivity() {
		millis = player.getCurrentPosition();
		player.stop();
	}

	private void resumeActivity() {
		hideError();
		if (player.getPlaybackState() == SimpleExoPlayer.STATE_IDLE) {
			player.prepare(videoSource);
			player.seekTo(millis);
			player.setPlayWhenReady(true);
		}
	}

	private void showError(int messageResId) {
		Log.e(TAG, getString(messageResId));

		videoView.setControllerHideOnTouch(false);
		showSystemUi();

		errorView.setText(messageResId);
		errorView.setVisibility(View.VISIBLE);
		loadingIndicator.setVisibility(View.INVISIBLE);
	}

	private void hideError() {
		videoView.setControllerHideOnTouch(true);
		errorView.setVisibility(View.GONE);
	}

	private void rewind() {
		player.seekTo(
			Math.max(player.getCurrentPosition() - PlaybackControlView.DEFAULT_REWIND_MS, 0)
		);
	}

	private void fastForward() {
		player.seekTo(
			Math.min(player.getCurrentPosition() + PlaybackControlView.DEFAULT_FAST_FORWARD_MS, player.getDuration())
		);
	}

	private void toggleSystemUi() {
		if (controlView.isVisible()) {
			controlView.hide();
		} else {
			controlView.show();
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
