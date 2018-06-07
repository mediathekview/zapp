package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.view.ViewPager
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import butterknife.BindDrawable
import butterknife.BindInt
import butterknife.OnTouch
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.livestream.ui.views.ProgramInfoViewBase
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity
import de.christinecoenen.code.zapp.model.ChannelModel
import de.christinecoenen.code.zapp.model.IChannelList
import de.christinecoenen.code.zapp.model.json.SortableJsonChannelList
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper
import de.christinecoenen.code.zapp.utils.system.NetworkConnectionHelper
import de.christinecoenen.code.zapp.utils.system.ShortcutHelper
import de.christinecoenen.code.zapp.utils.video.SwipeablePlayerView
import de.christinecoenen.code.zapp.utils.video.VideoBufferingHandler
import de.christinecoenen.code.zapp.utils.video.VideoErrorHandler
import de.christinecoenen.code.zapp.utils.view.ClickableViewPager
import de.christinecoenen.code.zapp.utils.view.ColorHelper
import de.christinecoenen.code.zapp.utils.view.FullscreenActivity
import kotlinx.android.synthetic.main.activity_channel_detail.*
import timber.log.Timber

class ChannelDetailActivity : FullscreenActivity(), VideoErrorHandler.IVideoErrorListener, VideoBufferingHandler.IVideoBufferingListener {

	private val viewPager: ClickableViewPager by lazy { viewpager_channels }
	private val videoView: SwipeablePlayerView by lazy { video }
	private val progressView: ProgressBar by lazy { progressbar_video }
	private val programInfoView: ProgramInfoViewBase by lazy { program_info }

	@BindDrawable(android.R.drawable.ic_media_pause)
	var pauseIcon: Drawable? = null

	@BindDrawable(android.R.drawable.ic_media_play)
	var playIcon: Drawable? = null

	@BindInt(R.integer.activity_channel_detail_play_stream_delay_millis)
	var playStreamDelayMillis: Int = 0

	private val playHandler = Handler()
	private val videoErrorHandler = VideoErrorHandler(this)
	private val bufferingHandler = VideoBufferingHandler(this)
	private val networkConnectionHelper = NetworkConnectionHelper(this)
	private lateinit var player: SimpleExoPlayer
	private lateinit var dataSourceFactory: DataSource.Factory
	private var currentChannel: ChannelModel? = null
	private var isPlaying = false
	private lateinit var channelDetailAdapter: ChannelDetailAdapter
	private lateinit var channelList: IChannelList

	private val playRunnable = Runnable { this.play() }

	private val onItemChangedListener = object : ChannelDetailAdapter.OnItemChangedListener {
		override fun onItemSelected(channelModel: ChannelModel) {
			currentChannel = channelModel
			title = channelModel.name
			setColor(channelModel.color)

			playDelayed()
			programInfoView.setChannel(channelModel)

			ShortcutHelper.reportShortcutUsageGuarded(this@ChannelDetailActivity, channelModel.id)
		}
	}

	private val onPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
		override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
			val color1 = channelDetailAdapter.getChannel(position).color

			if (positionOffset == 0f) {
				setColor(color1)
			} else {
				val color2 = channelDetailAdapter.getChannel(position + 1).color
				val color = ColorHelper.interpolate(positionOffset, color1, color2)
				setColor(color)
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setSupportActionBar(toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		channelList = SortableJsonChannelList(this)

		// player
		val bandwidthMeter = DefaultBandwidthMeter()
		val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
		val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
		dataSourceFactory = DefaultDataSourceFactory(this,
			Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter)
		player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
		player.addListener(bufferingHandler)
		player.addListener(videoErrorHandler)

		videoView.player = player
		videoView.setTouchOverlay(viewPager)

		// pager
		channelDetailAdapter = ChannelDetailAdapter(supportFragmentManager, channelList, onItemChangedListener)
		viewPager.adapter = channelDetailAdapter
		viewPager.addOnPageChangeListener(onPageChangeListener)
		viewPager.setOnClickListener { _ -> mContentView.performClick() }

		parseIntent(intent)

		networkConnectionHelper.startListenForNetworkChanges(this::onNetworkConnectionChanged)
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		// called when coming back from picture in picture mode
		isPlaying = false
		parseIntent(intent)
	}

	override fun onStart() {
		super.onStart()
		if (MultiWindowHelper.isInsideMultiWindow(this)) {
			resumeActivity()
		}
	}

	override fun onResume() {
		super.onResume()
		if (!MultiWindowHelper.isInsideMultiWindow(this)) {
			resumeActivity()
		}

		val preferences = PreferenceManager.getDefaultSharedPreferences(this)
		val lockScreen = preferences.getBoolean("pref_detail_landscape", true)

		requestedOrientation =
			if (lockScreen) {
				ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
			} else {
				ActivityInfo.SCREEN_ORIENTATION_SENSOR
			}
	}

	override fun onPause() {
		super.onPause()
		if (!MultiWindowHelper.isInsideMultiWindow(this)) {
			pauseActivity()
		}
	}

	override fun onStop() {
		super.onStop()
		if (MultiWindowHelper.isInsideMultiWindow(this)) {
			pauseActivity()
		}
	}

	override fun onDestroy() {
		super.onDestroy()

		player.removeListener(bufferingHandler)
		player.removeListener(videoErrorHandler)
		player.release()

		networkConnectionHelper.endListenForNetworkChanges()
	}

	override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, config: Configuration) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode, config)
		if (isInPictureInPictureMode) {
			hide()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.activity_channel_detail, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_share -> {
				startActivity(currentChannel!!.videoShareIntent)
				true
			}
			R.id.menu_settings -> {
				val settingsIntent = SettingsActivity.getStartIntent(this)
				startActivity(settingsIntent)
				true
			}
			android.R.id.home -> {
				finish()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
		val handled = when (keyCode) {
			KeyEvent.KEYCODE_DPAD_LEFT -> prevChannel()
			KeyEvent.KEYCODE_DPAD_RIGHT -> nextChannel()
			else -> false
		}

		return handled || super.onKeyUp(keyCode, event)
	}

	override fun onVideoError(messageResourceId: Int) {
		player.stop()
		progressView.visibility = View.GONE
		channelDetailAdapter.currentFragment?.onVideoError(getString(messageResourceId))
	}

	override fun onVideoErrorInvalid() {

	}

	override fun onBufferingStarted() {
		if (player.bufferedPercentage == 0) {
			progressView.visibility = View.VISIBLE
		}
	}

	override fun onBufferingEnded() {
		if (player.bufferedPercentage > 0 && progressView.visibility == View.VISIBLE) {
			progressView.visibility = View.GONE
			channelDetailAdapter.currentFragment?.onVideoStart()
		}
	}

	override fun getViewId(): Int {
		return R.layout.activity_channel_detail
	}

	@OnTouch(R.id.viewpager_channels)
	fun onPagerTouch(): Boolean {
		delayHide()
		return false
	}

	private fun onNetworkConnectionChanged() {
		if (networkConnectionHelper.isVideoPlaybackAllowed) {
			play()
		} else {
			onVideoError(R.string.error_stream_not_in_wifi)
		}
	}

	private fun parseIntent(intent: Intent) {
		val channelId = intent.extras.getString(EXTRA_CHANNEL_ID)
		val channelPosition = channelList.indexOf(channelId)

		viewPager.removeOnPageChangeListener(onPageChangeListener)
		viewPager.currentItem = channelPosition
		viewPager.addOnPageChangeListener(onPageChangeListener)
	}

	private fun pauseActivity() {
		programInfoView.pause()
		player.stop()
	}

	private fun resumeActivity() {
		programInfoView.resume()
		if (isPlaying) {
			play()
		}
	}

	private fun playDelayed() {
		player.playWhenReady = false
		progressView.visibility = View.VISIBLE
		playHandler.removeCallbacks(playRunnable)
		playHandler.postDelayed(playRunnable, playStreamDelayMillis.toLong())
	}

	private fun play() {
		if (!networkConnectionHelper.isVideoPlaybackAllowed) {
			onVideoError(R.string.error_stream_not_in_wifi)
			return
		}

		if (currentChannel == null) {
			return
		}

		Timber.d("play: %s", currentChannel?.name)
		isPlaying = true
		progressView.visibility = View.VISIBLE

		val videoUri = Uri.parse(currentChannel!!.streamUrl)
		val videoSource = HlsMediaSource.Factory(dataSourceFactory)
			.createMediaSource(videoUri, playHandler, videoErrorHandler)
		player.prepare(videoSource)
		player.playWhenReady = true
	}

	private fun prevChannel(): Boolean {
		val nextItemIndex = viewPager.currentItem - 1

		return if (nextItemIndex < 0) {
			false
		} else {
			delayHide()
			viewPager.setCurrentItem(nextItemIndex, true)
			true
		}
	}

	private fun nextChannel(): Boolean {
		val nextItemIndex = viewPager.currentItem + 1

		return if (nextItemIndex == channelDetailAdapter.count) {
			false
		} else {
			delayHide()
			viewPager.setCurrentItem(nextItemIndex, true)
			true
		}
	}

	private fun setColor(color: Int) {
		progressView.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
		toolbar.setBackgroundColor(color)

		val colorDarker = ColorHelper.darker(color, 0.075f)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window?.statusBarColor = colorDarker
		}

		val colorAlpha = ColorHelper.darker(ColorHelper.withAlpha(color, 150), 0.25f)
		mControlsView.setBackgroundColor(colorAlpha)
	}

	companion object {

		private const val EXTRA_CHANNEL_ID = "de.christinecoenen.code.zapp.EXTRA_CHANNEL_ID"

		@JvmStatic
		fun getStartIntent(context: Context, channelId: String): Intent {
			val intent = Intent(context, ChannelDetailActivity::class.java)
			intent.action = Intent.ACTION_VIEW
			intent.putExtra(EXTRA_CHANNEL_ID, channelId)
			return intent
		}
	}
}
