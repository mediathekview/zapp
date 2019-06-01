package de.christinecoenen.code.zapp.app.livestream.ui.detail;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindDrawable;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.OnTouch;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.livestream.ui.views.ProgramInfoViewBase;
import de.christinecoenen.code.zapp.app.player.BackgroundPlayerService;
import de.christinecoenen.code.zapp.app.player.Player;
import de.christinecoenen.code.zapp.app.player.VideoInfo;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.model.json.SortableJsonChannelList;
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper;
import de.christinecoenen.code.zapp.utils.system.ShortcutHelper;
import de.christinecoenen.code.zapp.utils.video.SwipeablePlayerView;
import de.christinecoenen.code.zapp.utils.view.ClickableViewPager;
import de.christinecoenen.code.zapp.utils.view.ColorHelper;
import de.christinecoenen.code.zapp.utils.view.FullscreenActivity;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class ChannelDetailActivity extends FullscreenActivity {

	private static final String EXTRA_CHANNEL_ID = "de.christinecoenen.code.zapp.EXTRA_CHANNEL_ID";


	@BindView(R.id.toolbar)
	protected Toolbar toolbar;

	@BindView(R.id.viewpager_channels)
	protected ClickableViewPager viewPager;

	@BindView(R.id.video)
	protected SwipeablePlayerView videoView;

	@BindView(R.id.progressbar_video)
	protected ProgressBar progressView;

	@BindView(R.id.program_info)
	protected ProgramInfoViewBase programInfoView;


	@BindDrawable(android.R.drawable.ic_media_pause)
	protected Drawable pauseIcon;

	@BindDrawable(android.R.drawable.ic_media_play)
	protected Drawable playIcon;

	@BindInt(R.integer.activity_channel_detail_play_stream_delay_millis)
	protected int playStreamDelayMillis;

	private final Handler playHandler = new Handler();
	private ChannelDetailAdapter channelDetailAdapter;
	private ChannelModel currentChannel;
	private Window window;
	private IChannelList channelList;
	private Player player;
	private BackgroundPlayerService.Binder binder;
	private SettingsRepository settings;
	private final CompositeDisposable disposable = new CompositeDisposable();

	private final Runnable playRunnable = this::play;

	private final ChannelDetailAdapter.OnItemChangedListener onItemChangedListener =
		new ChannelDetailAdapter.OnItemChangedListener() {
			@Override
			public void OnItemSelected(ChannelModel channel) {
				currentChannel = channel;
				setTitle(channel.getName());
				setColor(channel.getColor());

				playDelayed();
				programInfoView.setChannel(channel);

				ShortcutHelper.reportShortcutUsageGuarded(ChannelDetailActivity.this, channel.getId());
			}
		};

	private final ViewPager.OnPageChangeListener onPageChangeListener =
		new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				int color1 = channelDetailAdapter.getChannel(position).getColor();

				if (positionOffset == 0) {
					setColor(color1);
				} else {
					int color2 = channelDetailAdapter.getChannel(position + 1).getColor();
					int color = ColorHelper.interpolate(positionOffset, color1, color2);
					setColor(color);
				}
			}
		};

	private final ServiceConnection backgroundPlayerServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			binder = (BackgroundPlayerService.Binder) service;
			player = binder.getPlayer();
			player.setView(videoView);

			Disposable bufferingDisposable = player.isBuffering()
				.subscribe(ChannelDetailActivity.this::onBufferingChanged, Timber::e);
			Disposable errorDisposable = player.getErrorResourceId()
				.subscribe(ChannelDetailActivity.this::onVideoError, Timber::e);
			disposable.addAll(bufferingDisposable, errorDisposable);

			onItemChangedListener.OnItemSelected(currentChannel);

			binder.movePlaybackToForeground();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			player.pause();
		}
	};

	public static Intent getStartIntent(Context context, String channelId) {
		Intent intent = new Intent(context, ChannelDetailActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.putExtra(EXTRA_CHANNEL_ID, channelId);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSupportActionBar(toolbar);
		window = getWindow();

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		channelList = new SortableJsonChannelList(this);
		settings = new SettingsRepository(this);

		// pager
		channelDetailAdapter = new ChannelDetailAdapter(
			getSupportFragmentManager(), channelList, onItemChangedListener);
		viewPager.setAdapter(channelDetailAdapter);
		viewPager.addOnPageChangeListener(onPageChangeListener);
		viewPager.setOnClickListener(view -> mContentView.performClick());
		videoView.setTouchOverlay(viewPager);

		parseIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// called when coming back from picture in picture mode
		parseIntent(intent);
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
			hide();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_channel_detail, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_share:
				startActivity(Intent.createChooser(currentChannel.getVideoShareIntent(), getString(R.string.action_share)));
				return true;
			case R.id.menu_play_in_background:
				binder.movePlaybackToBackground();
				finish();
				return true;
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean handled = false;

		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				handled = prevChannel();
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				handled = nextChannel();
				break;
		}

		return handled || super.onKeyUp(keyCode, event);
	}

	private void onVideoError(Integer messageResourceId) {
		if (messageResourceId == null || messageResourceId == -1) {
			return;
		}

		player.pause();
		progressView.setVisibility(View.GONE);

		if (channelDetailAdapter.getCurrentFragment() != null) {
			channelDetailAdapter.getCurrentFragment().onVideoError(getString(messageResourceId));
		}
	}

	private void onBufferingChanged(boolean isBuffering) {
		if (isBuffering) {
			progressView.setVisibility(View.VISIBLE);
		} else {
			progressView.setVisibility(View.INVISIBLE);
			channelDetailAdapter.getCurrentFragment().onVideoStart();
		}
	}

	@Override
	protected int getViewId() {
		return R.layout.activity_channel_detail;
	}

	@SuppressWarnings("SameReturnValue")
	@OnTouch(R.id.viewpager_channels)
	public boolean onPagerTouch() {
		delayHide();
		return false;
	}

	private void parseIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		//noinspection ConstantConditions
		String channelId = extras.getString(EXTRA_CHANNEL_ID);
		int channelPosition = channelList.indexOf(channelId);

		viewPager.removeOnPageChangeListener(onPageChangeListener);
		viewPager.setCurrentItem(channelPosition);
		viewPager.addOnPageChangeListener(onPageChangeListener);
	}

	private void pauseActivity() {
		disposable.clear();
		unbindService(backgroundPlayerServiceConnection);
	}

	private void resumeActivity() {
		programInfoView.resume();
		BackgroundPlayerService.bind(this, backgroundPlayerServiceConnection, getIntent());
	}

	private void playDelayed() {
		if (player != null) {
			player.pause();
		}

		playHandler.removeCallbacks(playRunnable);
		playHandler.postDelayed(playRunnable, playStreamDelayMillis);
	}

	private void play() {
		if (currentChannel == null || binder == null) {
			return;
		}

		Intent currentIntent = ChannelDetailActivity.getStartIntent(this, currentChannel.getId());
		binder.updateForegroundActivityIntent(currentIntent);

		Timber.d("play: %s", currentChannel.getName());
		player.load(VideoInfo.fromChannel(currentChannel));
		player.resume();
	}

	private boolean prevChannel() {
		int nextItemIndex = viewPager.getCurrentItem() - 1;

		if (nextItemIndex < 0) {
			return false;
		} else {
			delayHide();
			viewPager.setCurrentItem(nextItemIndex, true);
			return true;
		}
	}

	private boolean nextChannel() {
		int nextItemIndex = viewPager.getCurrentItem() + 1;

		if (nextItemIndex == channelDetailAdapter.getCount()) {
			return false;
		} else {
			delayHide();
			viewPager.setCurrentItem(nextItemIndex, true);
			return true;
		}
	}

	private void setColor(int color) {
		progressView.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
		toolbar.setBackgroundColor(color);

		int colorDarker = ColorHelper.darker(color, 0.075f);
		window.setStatusBarColor(colorDarker);

		int colorAlpha = ColorHelper.darker(ColorHelper.withAlpha(color, 150), 0.25f);
		mControlsView.setBackgroundColor(colorAlpha);
	}
}
