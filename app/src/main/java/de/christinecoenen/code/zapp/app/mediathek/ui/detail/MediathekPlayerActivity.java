package de.christinecoenen.code.zapp.app.mediathek.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ui.PlayerControlView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.controller.Player;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper;
import de.christinecoenen.code.zapp.utils.video.SwipeablePlayerView;
import de.christinecoenen.code.zapp.utils.video.VideoBufferingHandler;
import de.christinecoenen.code.zapp.utils.video.VideoErrorHandler;
import timber.log.Timber;

public class MediathekPlayerActivity extends AppCompatActivity implements
	PlayerControlView.VisibilityListener,
	VideoErrorHandler.IVideoErrorListener,
	VideoBufferingHandler.IVideoBufferingListener {

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
	protected SwipeablePlayerView videoView;

	@BindView(R.id.btn_caption_enable)
	protected ImageButton captionButtonEnable;

	@BindView(R.id.btn_caption_disable)
	protected ImageButton captionButtonDisable;

	@BindView(R.id.text_error)
	protected TextView errorView;

	@BindView(R.id.progress)
	protected ProgressBar loadingIndicator;

	private MediathekShow show;
	private Player player;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_mediathek_player);
		ButterKnife.bind(this);

		// set to show
		//noinspection ConstantConditions
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


		player = new Player(this, show, this, this);
		player.setView(videoView);
		updateSubtitleButtons();

		videoView.setControllerVisibilityListener(this);
		videoView.requestFocus();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ARG_VIDEO_MILLIS, player.getMillis());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		player.setMillis(savedInstanceState.getLong(ARG_VIDEO_MILLIS));
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
		player.destroy();
	}

	@Override
	public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode);
		if (isInPictureInPictureMode) {
			videoView.hideControls();
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
				player.rewind();
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_MEDIA_STEP_FORWARD:
			case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				player.fastForward();
				return true;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_MEDIA_TOP_MENU:
				videoView.toggleControls();
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
	public void onVideoErrorInvalid() {
		hideError();
	}

	@Override
	public void onBufferingStarted() {
		loadingIndicator.setVisibility(View.VISIBLE);
	}

	@Override
	public void onBufferingEnded() {
		loadingIndicator.setVisibility(View.INVISIBLE);
	}

	@OnClick(R.id.btn_caption_disable)
	public void onDisableCaptionsClick() {
		player.disableSubtitles();
		updateSubtitleButtons();
	}

	@OnClick(R.id.btn_caption_enable)
	public void onEnableCaptionsClick() {
		player.enableSubtitles();
		updateSubtitleButtons();
	}

	private void pauseActivity() {
		player.pause();
	}

	private void resumeActivity() {
		hideError();
		player.resume();
	}

	private void updateSubtitleButtons() {
		captionButtonEnable.setVisibility(player.hasSubtitles() && !player.isShowingSubtitles() ? View.VISIBLE : View.GONE);
		captionButtonDisable.setVisibility(player.isShowingSubtitles() ? View.VISIBLE : View.GONE);
	}

	private void showError(int messageResId) {
		Timber.e(getString(messageResId));

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
