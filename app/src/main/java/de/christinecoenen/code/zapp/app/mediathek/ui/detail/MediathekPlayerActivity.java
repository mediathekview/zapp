package de.christinecoenen.code.zapp.app.mediathek.ui.detail;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ui.StyledPlayerControlView;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.ZappApplication;
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekRepository;
import de.christinecoenen.code.zapp.app.player.BackgroundPlayerService;
import de.christinecoenen.code.zapp.app.player.Player;
import de.christinecoenen.code.zapp.app.player.VideoInfo;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.databinding.ActivityMediathekPlayerBinding;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper;
import de.christinecoenen.code.zapp.utils.video.SwipeablePlayerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class MediathekPlayerActivity extends AppCompatActivity implements
	StyledPlayerControlView.VisibilityListener {

	private static final String EXTRA_PERSISTED_SHOW_ID = "de.christinecoenen.code.zapp.EXTRA_PERSISTED_SHOW_ID";

	public static Intent getStartIntent(Context context, int persistedShowId) {
		Intent intent = new Intent(context, MediathekPlayerActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.putExtra(EXTRA_PERSISTED_SHOW_ID, persistedShowId);
		return intent;
	}


	private View fullscreenContent;
	private SwipeablePlayerView videoView;
	private TextView errorView;


	private final CompositeDisposable pauseDisposables = new CompositeDisposable();
	private int persistedShowId;
	private PersistedMediathekShow persistedShow;
	private Player player;
	private MediathekRepository mediathekRepository;
	private SettingsRepository settings;
	private BackgroundPlayerService.Binder binder;

	private final ServiceConnection backgroundPlayerServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			binder = (BackgroundPlayerService.Binder) service;
			binder.setForegroundActivityIntent(getIntent());
			player = binder.getPlayer();
			player.setView(videoView);

			Disposable loadShowDisposable = mediathekRepository.getPersistedShow(persistedShowId)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(MediathekPlayerActivity.this::onShowLoaded, Timber::e);

			pauseDisposables.add(loadShowDisposable);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			player.pause();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActivityMediathekPlayerBinding binding = ActivityMediathekPlayerBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		fullscreenContent = binding.fullscreenContent;
		videoView = binding.video;
		errorView = binding.error;

		setSupportActionBar(binding.toolbar);

		// set to show
		parseIntent(getIntent());

		settings = new SettingsRepository(this);
		mediathekRepository = ((ZappApplication) getApplicationContext()).getMediathekRepository();

		videoView.setControllerVisibilityListener(this);
		videoView.requestFocus();

		errorView.setOnClickListener(this::onErrorViewClick);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// called when coming back from picture in picture mode
		parseIntent(intent);
	}

	private void onErrorViewClick(View view) {
		hideError();
		player.recreate();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (MultiWindowHelper.isInsideMultiWindow(this)) {
			resumeActivity();
		}
	}

	@SuppressLint("SourceLockedOrientationActivity")
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
		pauseActivity();
	}

	@Override
	public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode);
		if (isInPictureInPictureMode) {
			videoView.setUseController(false);
		} else {
			videoView.setUseController(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_mediathek_player, menu);

		if (!MultiWindowHelper.supportsPictureInPictureMode(this)) {
			menu.removeItem(R.id.menu_pip);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_share:
				IntentHelper.openUrl(this, persistedShow.getMediathekShow().getVideoUrl(Quality.Medium));
				return true;
			case R.id.menu_play_in_background:
				binder.movePlaybackToBackground();
				finish();
				return true;
			case R.id.menu_pip:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					enterPictureInPictureMode();
				}
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
			case KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD:
			case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				player.rewind();
				return true;
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

	private void onShowLoaded(PersistedMediathekShow persistedMediathekShow) {
		this.persistedShow = persistedMediathekShow;

		if (getSupportActionBar() != null) {
			setTitle(persistedShow.getMediathekShow().getTopic());
			getSupportActionBar().setSubtitle(persistedShow.getMediathekShow().getTitle());
		}

		VideoInfo videoInfo = VideoInfo.fromShow(persistedShow);
		player.load(videoInfo);
		player.resume();

		Disposable errorDisposable = player.getErrorResourceId()
			.subscribe(MediathekPlayerActivity.this::onVideoError, Timber::e);
		pauseDisposables.addAll(errorDisposable);

		binder.movePlaybackToForeground();

		boolean isInPipMode = MultiWindowHelper.isInPictureInPictureMode(MediathekPlayerActivity.this);
		onPictureInPictureModeChanged(isInPipMode);
	}

	private void onVideoError(Integer messageResourceId) {
		if (messageResourceId == null || messageResourceId == -1) {
			hideError();
		} else {
			showError(messageResourceId);
		}
	}

	private void pauseActivity() {
		pauseDisposables.clear();
		try {
			unbindService(backgroundPlayerServiceConnection);
		} catch (IllegalArgumentException ignored) {

		}
	}

	private void parseIntent(Intent intent) {
		//noinspection ConstantConditions
		persistedShowId = intent.getExtras().getInt(EXTRA_PERSISTED_SHOW_ID, 0);
		if (persistedShowId == 0) {
			Toast.makeText(this, R.string.error_mediathek_called_without_show, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private void resumeActivity() {
		hideError();
		BackgroundPlayerService.bind(this, backgroundPlayerServiceConnection);
	}

	private void showError(int messageResId) {
		Timber.e(getString(messageResId));

		videoView.setControllerHideOnTouch(false);
		showSystemUi();

		errorView.setText(messageResId);
		errorView.setVisibility(View.VISIBLE);
	}

	private void hideError() {
		videoView.setControllerHideOnTouch(true);
		errorView.setVisibility(View.GONE);
	}

	private void showSystemUi() {
		if (getSupportActionBar() != null) {
			getSupportActionBar().show();
		}

		fullscreenContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
