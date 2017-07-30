package de.christinecoenen.code.zapp.app.livestream.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import butterknife.BindDrawable;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.OnTouch;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.livestream.ui.views.ProgramInfoViewBase;
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.model.json.SortableJsonChannelList;
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper;
import de.christinecoenen.code.zapp.utils.system.ShortcutHelper;
import de.christinecoenen.code.zapp.utils.video.VideoBufferingHandler;
import de.christinecoenen.code.zapp.utils.video.VideoErrorHandler;
import de.christinecoenen.code.zapp.utils.view.ClickableViewPager;
import de.christinecoenen.code.zapp.utils.view.ColorHelper;
import de.christinecoenen.code.zapp.utils.view.FullscreenActivity;
import timber.log.Timber;

public class ChannelDetailActivity extends FullscreenActivity implements
	VideoErrorHandler.IVideoErrorListener, VideoBufferingHandler.IVideoBufferingListener {

	private static final String EXTRA_CHANNEL_ID = "de.christinecoenen.code.zapp.EXTRA_CHANNEL_ID";


	@BindView(R.id.toolbar)
	protected Toolbar toolbar;

	@BindView(R.id.viewpager_channels)
	protected ClickableViewPager viewPager;

	@BindView(R.id.video)
	protected SimpleExoPlayerView videoView;

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
	private final VideoErrorHandler videoErrorHandler = new VideoErrorHandler(this);
	private final VideoBufferingHandler bufferingHandler = new VideoBufferingHandler(this);
	private SimpleExoPlayer player;
	private DataSource.Factory dataSourceFactory;
	private ChannelDetailAdapter channelDetailAdapter;
	private ChannelModel currentChannel;
	private boolean isPlaying = false;
	private Window window;
	private IChannelList channelList;

	private final Runnable playRunnable = new Runnable() {
		@Override
		public void run() {
			play();
		}
	};

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

		// player
		DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
		TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
		dataSourceFactory = new DefaultDataSourceFactory(this,
			Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter);
		player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
		player.addListener(bufferingHandler);
		player.addListener(videoErrorHandler);
		videoView.setPlayer(player);

		// pager
		channelDetailAdapter = new ChannelDetailAdapter(
			getSupportFragmentManager(), channelList, onItemChangedListener);
		viewPager.setAdapter(channelDetailAdapter);
		viewPager.addOnPageChangeListener(onPageChangeListener);
		viewPager.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mContentView.performClick();
			}
		});

		parseIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// called when coming back from picture in picture mode
		isPlaying = false;
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

		player.removeListener(bufferingHandler);
		player.removeListener(videoErrorHandler);
		player.release();
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
				Intent videoIntent = new Intent(Intent.ACTION_VIEW);
				videoIntent.setDataAndType(Uri.parse(currentChannel.getStreamUrl()), "video/*");
				startActivity(videoIntent);
				return true;
			case R.id.menu_settings:
				Intent settingsIntent = SettingsActivity.getStartIntent(this);
				startActivity(settingsIntent);
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

	@Override
	public void onVideoError(int messageResourceId) {
		player.stop();
		progressView.setVisibility(View.GONE);
		channelDetailAdapter.getCurrentFragment().onVideoError(getString(messageResourceId));
	}

	@Override
	public void onBufferingStarted() {
		progressView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onBufferingEnded() {
		progressView.setVisibility(View.GONE);
		channelDetailAdapter.getCurrentFragment().onVideoStart();
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
		String channelId = extras.getString(EXTRA_CHANNEL_ID);
		int channelPosition = channelList.indexOf(channelId);

		viewPager.removeOnPageChangeListener(onPageChangeListener);
		viewPager.setCurrentItem(channelPosition);
		viewPager.addOnPageChangeListener(onPageChangeListener);
	}

	private void pauseActivity() {
		programInfoView.pause();
		player.stop();
	}

	private void resumeActivity() {
		programInfoView.resume();
		if (isPlaying) {
			play();
		}
	}

	private void playDelayed() {
		player.setPlayWhenReady(false);
		progressView.setVisibility(View.VISIBLE);
		playHandler.removeCallbacks(playRunnable);
		playHandler.postDelayed(playRunnable, playStreamDelayMillis);
	}

	private void play() {
		Timber.d("play: " + currentChannel.getName());
		isPlaying = true;
		progressView.setVisibility(View.VISIBLE);

		Uri videoUri = Uri.parse(currentChannel.getStreamUrl());
		MediaSource videoSource = new HlsMediaSource(videoUri, dataSourceFactory, playHandler, videoErrorHandler);
		player.prepare(videoSource);
		player.setPlayWhenReady(true);
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.setStatusBarColor(colorDarker);
		}

		int colorAlpha = ColorHelper.darker(ColorHelper.withAlpha(color, 150), 0.25f);
		mControlsView.setBackgroundColor(colorAlpha);
	}
}
