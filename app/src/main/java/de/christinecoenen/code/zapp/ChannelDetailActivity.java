package de.christinecoenen.code.zapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.VideoView;

import butterknife.BindDrawable;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.OnTouch;
import de.christinecoenen.code.zapp.adapters.ChannelDetailAdapter;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.model.json.SortableJsonChannelList;
import de.christinecoenen.code.zapp.utils.ColorHelper;
import de.christinecoenen.code.zapp.utils.MultiWindowHelper;
import de.christinecoenen.code.zapp.utils.VideoErrorHandler;
import de.christinecoenen.code.zapp.utils.view.ClickableViewPager;
import de.christinecoenen.code.zapp.utils.view.FullscreenActivity;
import de.christinecoenen.code.zapp.views.ProgramInfoViewBase;

public class ChannelDetailActivity extends FullscreenActivity implements
		VideoErrorHandler.IVideoErrorListener {

	private static final String TAG = ChannelDetailActivity.class.getSimpleName();
	private static final String EXTRA_CHANNEL_ID = "de.christinecoenen.code.zapp.EXTRA_CHANNEL_ID";

	protected @BindView(R.id.toolbar) Toolbar toolbar;
	protected @BindView(R.id.viewpager_channels) ClickableViewPager viewPager;
	protected @BindView(R.id.video) VideoView videoView;
	protected @BindView(R.id.progressbar_video) ProgressBar progressView;
	protected @BindView(R.id.program_info) ProgramInfoViewBase programInfoView;

	protected @BindDrawable(android.R.drawable.ic_media_pause) Drawable pauseIcon;
	protected @BindDrawable(android.R.drawable.ic_media_play) Drawable playIcon;

	protected @BindInt(R.integer.activity_channel_detail_play_stream_delay_millis) int playStreamDelayMillis;

	private final Handler playHandler = new Handler();
	private ChannelDetailAdapter channelDetailAdapter;
	private ChannelModel currentChannel;
	private boolean isPlaying = false;
	private Window window;

	private final Runnable playRunnable = new Runnable() {
		@Override
		public void run() {
			play();
		}
	};

	private final MediaPlayer.OnInfoListener videoInfoListener = new MediaPlayer.OnInfoListener() {
		@Override
		public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
			switch (what) {
				case MediaPlayer.MEDIA_INFO_BUFFERING_START:
					Log.d(TAG, "media player buffering start");
					progressView.setVisibility(View.VISIBLE);
					return true;
				case MediaPlayer.MEDIA_INFO_BUFFERING_END:
					Log.d(TAG, "media player buffering end");
					progressView.setVisibility(View.GONE);
					return true;
				case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
					Log.d(TAG, "media player rendering start");
					progressView.setVisibility(View.GONE);
					channelDetailAdapter.getCurrentFragment().onVideoStart();
					return true;
				default:
					return false;
			}
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

		final IChannelList channelList = new SortableJsonChannelList(this);

		// set to channel
		Bundle extras = getIntent().getExtras();
		String channelId = extras.getString(EXTRA_CHANNEL_ID);
		int channelPosition = channelList.indexOf(channelId);

		// listener
		videoView.setOnErrorListener(new VideoErrorHandler(this));
		videoView.setOnInfoListener(videoInfoListener);

		// pager
		channelDetailAdapter = new ChannelDetailAdapter(
				getSupportFragmentManager(), channelList, onItemChangedListener);
		viewPager.setAdapter(channelDetailAdapter);
		viewPager.setCurrentItem(channelPosition);
		viewPager.addOnPageChangeListener(onPageChangeListener);
		viewPager.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mContentView.performClick();
			}
		});
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_channel_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_settings:
				Intent videoIntent = new Intent(Intent.ACTION_VIEW);
				videoIntent.setDataAndType(Uri.parse(currentChannel.getStreamUrl()), "video/*");
				startActivity(videoIntent);
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
	public boolean onVideoError(int messageResourceId) {
		progressView.setVisibility(View.GONE);

		String message = getString(messageResourceId);
		channelDetailAdapter.getCurrentFragment().onVideoError(message);

		return true;
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

	private void pauseActivity() {
		programInfoView.pause();
		videoView.stopPlayback();
	}

	private void resumeActivity() {
		programInfoView.resume();
		if (isPlaying) {
			play();
		}
	}

	private void playDelayed() {
		videoView.pause();
		progressView.setVisibility(View.VISIBLE);
		playHandler.removeCallbacks(playRunnable);
		playHandler.postDelayed(playRunnable, playStreamDelayMillis);
	}

	private void play() {
		Log.d(TAG, "play: " + currentChannel.getName());
		isPlaying = true;
		progressView.setVisibility(View.VISIBLE);
		videoView.setVideoPath(currentChannel.getStreamUrl());
		videoView.start();
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
