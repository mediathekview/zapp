package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.*
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.ZappApplicationBase
import de.christinecoenen.code.zapp.app.player.BackgroundPlayerService
import de.christinecoenen.code.zapp.app.player.BackgroundPlayerService.Companion.bind
import de.christinecoenen.code.zapp.app.player.Player
import de.christinecoenen.code.zapp.app.player.VideoInfo.Companion.fromChannel
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.databinding.ActivityChannelDetailBinding
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper.isInsideMultiWindow
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper.supportsPictureInPictureMode
import de.christinecoenen.code.zapp.utils.system.ShortcutHelper.reportShortcutUsageGuarded
import de.christinecoenen.code.zapp.utils.view.ColorHelper.darker
import de.christinecoenen.code.zapp.utils.view.ColorHelper.interpolate
import de.christinecoenen.code.zapp.utils.view.ColorHelper.withAlpha
import de.christinecoenen.code.zapp.utils.view.FullscreenActivity
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


class ChannelDetailActivity : FullscreenActivity(), StreamPageFragment.Listener {

	companion object {

		private const val EXTRA_CHANNEL_ID = "de.christinecoenen.code.zapp.EXTRA_CHANNEL_ID"

		@JvmStatic
		fun getStartIntent(context: Context?, channelId: String?): Intent {
			return Intent(context, ChannelDetailActivity::class.java).apply {
				action = Intent.ACTION_VIEW
				putExtra(EXTRA_CHANNEL_ID, channelId)
			}
		}

	}

	private val viewModel: ChannelDetailActivityViewModel by viewModels {
		val application = applicationContext as ZappApplicationBase
		ChannelDetailActivityViewModelFactory(
			application.channelRepository,
			SettingsRepository(this)
		)
	}

	private lateinit var binding: ActivityChannelDetailBinding
	private lateinit var channelDetailAdapter: ChannelDetailAdapter

	private val disposable = CompositeDisposable()
	private val playRunnable = Runnable { play() }
	private val playHandler = Handler()

	private var playStreamDelayMillis = 0
	private var currentChannel: ChannelModel? = null
	private var player: Player? = null
	private var binder: BackgroundPlayerService.Binder? = null

	private val channelDetailListener: ChannelDetailAdapter.Listener = object : ChannelDetailAdapter.Listener {
		override fun onItemSelected(channel: ChannelModel) {
			currentChannel = channel
			title = channel.name

			setColor(channel.color)
			playDelayed()

			binding.programInfo.setChannel(channel)

			reportShortcutUsageGuarded(this@ChannelDetailActivity, channel.id)
		}
	}

	private val onPageChangeListener: OnPageChangeListener = object : SimpleOnPageChangeListener() {
		override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
			val currentChannelColor = channelDetailAdapter.getChannel(position).color

			if (positionOffset == 0f) {
				setColor(currentChannelColor)
			} else {
				val nextChannelColor = channelDetailAdapter.getChannel(position + 1).color
				val interpolatedColor = interpolate(positionOffset, currentChannelColor, nextChannelColor)
				setColor(interpolatedColor)
			}
		}
	}

	private val backgroundPlayerServiceConnection: ServiceConnection = object : ServiceConnection {
		override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
			binder = service as BackgroundPlayerService.Binder
			binder!!.setForegroundActivityIntent(intent)

			player = binder!!.getPlayer()
			player!!.setView(binding.video)

			player!!.isBuffering
				.subscribe(::onBufferingChanged, Timber::e)
				.also(disposable::add)

			player!!.errorResourceId
				.subscribe(::onVideoError, Timber::e)
				.also(disposable::add)

			channelDetailListener.onItemSelected(currentChannel!!)

			binder!!.movePlaybackToForeground()
		}

		override fun onServiceDisconnected(componentName: ComponentName) {
			player?.pause()
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityChannelDetailBinding.inflate(layoutInflater)
		setContentView(binding.root)

		playStreamDelayMillis = resources.getInteger(R.integer.activity_channel_detail_play_stream_delay_millis)

		setSupportActionBar(binding.toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		// pager
		channelDetailAdapter = ChannelDetailAdapter(
			supportFragmentManager, viewModel.channelList, channelDetailListener)

		binding.viewpager.adapter = channelDetailAdapter
		binding.viewpager.addOnPageChangeListener(onPageChangeListener)
		binding.viewpager.setOnClickListener { fullscreenContent.performClick() }
		binding.viewpager.setOnTouchListener { _, _ ->
			delayHide()
			false
		}

		binding.video.setTouchOverlay(binding.viewpager)

		parseIntent(intent)
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)

		// called when coming back from picture in picture mode
		parseIntent(intent)
	}

	override fun onStart() {
		super.onStart()

		if (isInsideMultiWindow(this)) {
			resumeActivity()
		}
	}

	@SuppressLint("SourceLockedOrientationActivity")
	override fun onResume() {
		super.onResume()

		if (!isInsideMultiWindow(this)) {
			resumeActivity()
		}

		requestedOrientation = viewModel.screenOrientation
	}

	override fun onPause() {
		super.onPause()

		if (!isInsideMultiWindow(this)) {
			pauseActivity()
		}
	}

	override fun onStop() {
		super.onStop()

		pauseActivity()
	}

	override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

		if (isInPictureInPictureMode) {
			hide()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.activity_channel_detail, menu)

		if (!supportsPictureInPictureMode(this)) {
			menu.removeItem(R.id.menu_pip)
		}

		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_share -> {
				startActivity(Intent.createChooser(currentChannel!!.videoShareIntent, getString(R.string.action_share)))
				true
			}
			R.id.menu_play_in_background -> {
				binder!!.movePlaybackToBackground()
				finish()
				true
			}
			R.id.menu_pip -> {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					MultiWindowHelper.enterPictureInPictureMode(this)
				}
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

	private fun onVideoError(messageResourceId: Int?) {
		if (messageResourceId == null || messageResourceId == -1) {
			return
		}

		player?.pause()
		binding.videoProgress.isVisible = false
		channelDetailAdapter.currentFragment?.onVideoError(getString(messageResourceId))
	}

	private fun onBufferingChanged(isBuffering: Boolean) {
		if (isBuffering) {
			binding.videoProgress.isVisible = true
		} else if (!player!!.isIdle) {
			binding.videoProgress.isVisible = false
			channelDetailAdapter.currentFragment?.onVideoStart()
		}
	}

	override fun onErrorViewClicked() {
		player?.recreate()
		player?.resume()
	}

	private fun parseIntent(intent: Intent) {

		val channelId = intent.extras?.getString(EXTRA_CHANNEL_ID)
			?: throw IllegalArgumentException("Channel id is not allowed to be null.")

		val channelPosition = viewModel.getChannelPosition(channelId)

		binding.viewpager.removeOnPageChangeListener(onPageChangeListener)
		binding.viewpager.currentItem = channelPosition
		binding.viewpager.addOnPageChangeListener(onPageChangeListener)
	}

	private fun pauseActivity() {
		disposable.clear()

		try {
			unbindService(backgroundPlayerServiceConnection)
		} catch (ignored: IllegalArgumentException) {
		}
	}

	private fun resumeActivity() {
		binding.programInfo.resume()
		bind(this, backgroundPlayerServiceConnection)
	}

	private fun playDelayed() {
		if (player != null) {
			player!!.pause()
		}

		playHandler.removeCallbacks(playRunnable)
		playHandler.postDelayed(playRunnable, playStreamDelayMillis.toLong())
	}

	private fun play() {
		if (currentChannel == null || binder == null) {
			return
		}

		val currentIntent = getStartIntent(this, currentChannel!!.id)
		binder!!.setForegroundActivityIntent(currentIntent)

		Timber.d("play: %s", currentChannel!!.name)
		player!!.load(fromChannel(currentChannel!!))
		player!!.resume()
	}

	private fun prevChannel(): Boolean {
		val nextItemIndex = binding.viewpager.currentItem - 1

		return if (nextItemIndex < 0) {
			false
		} else {
			delayHide()
			binding.viewpager.setCurrentItem(nextItemIndex, true)
			true
		}
	}

	private fun nextChannel(): Boolean {
		val nextItemIndex = binding.viewpager.currentItem + 1

		return if (nextItemIndex == channelDetailAdapter.count) {
			false
		} else {
			delayHide()
			binding.viewpager.setCurrentItem(nextItemIndex, true)
			true
		}
	}

	private fun setColor(color: Int) {
		binding.videoProgress.indeterminateDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
		binding.toolbar.setBackgroundColor(color)

		window.statusBarColor = darker(color, 0.075f)

		val colorAlpha = darker(withAlpha(color, 150), 0.25f)
		controlsView.setBackgroundColor(colorAlpha)
	}
}
