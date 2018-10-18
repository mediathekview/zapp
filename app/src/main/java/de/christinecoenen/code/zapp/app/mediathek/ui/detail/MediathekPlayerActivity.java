package de.christinecoenen.code.zapp.app.mediathek.ui.detail;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ui.PlayerControlView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.player.BackgroundPlayerService;
import de.christinecoenen.code.zapp.app.player.Player;
import de.christinecoenen.code.zapp.app.player.VideoInfo;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper;
import de.christinecoenen.code.zapp.utils.video.SwipeablePlayerView;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class MediathekPlayerActivity extends AppCompatActivity implements
	PlayerControlView.VisibilityListener {

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

	private final CompositeDisposable disposable = new CompositeDisposable();
	private MediathekShow show;
	private Player player;
	private long lastPlayerMillis;
	private SettingsRepository settings;
	private BackgroundPlayerService.Binder binder;

	private final ServiceConnection backgroundPlayerServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			binder = (BackgroundPlayerService.Binder) service;
			player = binder.getPlayer();
			player.setView(videoView);

			player.load(VideoInfo.fromShow(show));
			if (player.getMillis() == 0) {
				player.setMillis(lastPlayerMillis);
			}
			player.resume();

			Disposable bufferingDisposable = player.isBuffering()
				.subscribe(MediathekPlayerActivity.this::onBufferingChanged, Timber::e);
			Disposable errorDisposable = player.getErrorResourceId()
				.subscribe(MediathekPlayerActivity.this::onVideoError, Timber::e);
			disposable.addAll(bufferingDisposable, errorDisposable);

			binder.movePlaybackToForeground();

			updateSubtitleButtons();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			player.pause();
		}
	};

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

		settings = new SettingsRepository(this);

		videoView.setControllerVisibilityListener(this);
		videoView.requestFocus();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ARG_VIDEO_MILLIS, getPlayerMillisSafe());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		lastPlayerMillis = savedInstanceState.getLong(ARG_VIDEO_MILLIS);
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

		if (settings.getLockVideosInLandcapeFormat()) {
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

	private void onVideoError(Integer messageResourceId) {
		if (messageResourceId == null) {
			hideError();
		} else {
			showError(messageResourceId);
		}
	}

	private void onBufferingChanged(boolean isBuffering) {
		loadingIndicator.setVisibility(isBuffering ? View.VISIBLE : View.INVISIBLE);
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

	@OnClick(R.id.btn_background)
	public void onBackgroundClick() {
		binder.movePlaybackToBackground();
		finish();
	}

	private void pauseActivity() {
		lastPlayerMillis = getPlayerMillisSafe();
		disposable.clear();
		unbindService(backgroundPlayerServiceConnection);
	}

	private void resumeActivity() {
		hideError();
		BackgroundPlayerService.bind(this, backgroundPlayerServiceConnection, getIntent());
	}

	private void updateSubtitleButtons() {
		captionButtonEnable.setVisibility(player.getCurrentVideoInfo().hasSubtitles() && !player.isShowingSubtitles() ? View.VISIBLE : View.GONE);
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

	private long getPlayerMillisSafe() {
		return player == null ? 0 : player.getMillis();
	}
}
