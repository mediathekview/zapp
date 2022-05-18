package de.christinecoenen.code.zapp.app.player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.player.BackgroundPlayerService.Companion.bind
import de.christinecoenen.code.zapp.databinding.ActivityAbstractPlayerBinding
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper.isInsideMultiWindow
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper.supportsPictureInPictureMode
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

abstract class AbstractPlayerActivity :
	AppCompatActivity(), StyledPlayerControlView.VisibilityListener {

	private val viewModel: AbstractPlayerActivityViewModel by viewModel()

	private val windowInsetsControllerCompat by lazy {
		WindowInsetsControllerCompat(window, binding.fullscreenContent)
	}

	protected lateinit var binding: ActivityAbstractPlayerBinding

	protected var player: Player? = null
	protected var binder: BackgroundPlayerService.Binder? = null

	private val backgroundPlayerServiceConnection: ServiceConnection = object : ServiceConnection {

		override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
			binder = service as BackgroundPlayerService.Binder
			binder!!.setForegroundActivityIntent(intent)

			player = binder!!.getPlayer()
			player!!.setView(binding.video)

			lifecycleScope.launchWhenResumed {
				player!!.errorResourceId.collect(::onVideoError)
			}

			loadVideoFromIntent(intent)
		}

		override fun onServiceDisconnected(componentName: ComponentName) {
			player?.pause()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityAbstractPlayerBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(binding.toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		// set to show
		parseIntent(intent)

		binding.video.setControllerVisibilityListener(this)
		binding.video.requestFocus()
		binding.error.setOnClickListener { onErrorViewClick() }
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)

		// called when coming back from picture in picture mode
		parseIntent(intent)
	}

	private fun onErrorViewClick() {
		hideError()

		lifecycleScope.launchWhenResumed {
			player?.recreate()
		}
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

	override fun onPictureInPictureModeChanged(
		isInPictureInPictureMode: Boolean,
		newConfig: Configuration
	) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

		handlePictureInPictureModeChanged(isInPictureInPictureMode)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.activity_abstract_player, menu)

		if (!supportsPictureInPictureMode(this)) {
			menu.removeItem(R.id.menu_pip)
		}

		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_share -> {
				onShareMenuItemClicked()
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
		return when (keyCode) {
			KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD, KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD, KeyEvent.KEYCODE_MEDIA_REWIND -> {
				player?.rewind()
				true
			}
			KeyEvent.KEYCODE_MEDIA_STEP_FORWARD, KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
				player?.fastForward()
				true
			}
			KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_MEDIA_TOP_MENU -> {
				binding.video.toggleControls()
				true
			}
			KeyEvent.KEYCODE_MEDIA_PLAY -> {
				resumeActivity()
				true
			}
			KeyEvent.KEYCODE_MEDIA_PAUSE -> {
				pauseActivity()
				true
			}
			KeyEvent.KEYCODE_MEDIA_CLOSE -> {
				finish()
				true
			}
			else -> super.onKeyUp(keyCode, event)
		}
	}

	override fun onVisibilityChange(visibility: Int) {
		if (binding.video.isControllerFullyVisible) {
			showSystemUi()
		} else {
			hideSystemUi()
		}
	}

	abstract fun onShareMenuItemClicked()

	abstract suspend fun getVideoInfoFromIntent(intent: Intent): VideoInfo

	private fun parseIntent(intent: Intent) {
		if (player == null) {
			// intent will be parsed later when player service is connected
			return
		}

		loadVideoFromIntent(intent)
	}

	private fun loadVideoFromIntent(intent: Intent) {
		lifecycleScope.launchWhenCreated {
			load(getVideoInfoFromIntent(intent))
		}
	}

	private suspend fun load(videoInfo: VideoInfo) {
		player!!.load(videoInfo)
		player!!.resume()

		binder!!.movePlaybackToForeground()

		val isInPipMode = MultiWindowHelper.isInPictureInPictureMode(this)
		handlePictureInPictureModeChanged(isInPipMode)
	}

	private fun onVideoError(messageResourceId: Int?) {
		if (messageResourceId == null || messageResourceId == -1) {
			hideError()
		} else {
			showError(messageResourceId)
		}
	}

	private fun pauseActivity() {
		try {
			unbindService(backgroundPlayerServiceConnection)
		} catch (ignored: IllegalArgumentException) {
		}
	}

	private fun resumeActivity() {
		hideError()

		windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars())
		windowInsetsControllerCompat.systemBarsBehavior =
			WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

		bind(this, backgroundPlayerServiceConnection)
	}

	private fun handlePictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
		binding.video.useController = !isInPictureInPictureMode

		if (!isInPictureInPictureMode) {
			binding.video.showController()
		} else {
			hideSystemUi()
		}
	}

	private fun showError(messageResId: Int) {
		Timber.e(getString(messageResId))

		showSystemUi()

		binding.video.controllerHideOnTouch = false
		binding.error.setText(messageResId)
		binding.error.visibility = View.VISIBLE
	}

	private fun hideError() {
		binding.video.controllerHideOnTouch = true
		binding.error.visibility = View.GONE
	}

	private fun showSystemUi() {
		supportActionBar?.show()
	}

	private fun hideSystemUi() {
		supportActionBar?.hide()
	}
}
