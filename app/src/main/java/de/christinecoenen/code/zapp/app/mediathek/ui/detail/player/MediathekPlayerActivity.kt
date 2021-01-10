package de.christinecoenen.code.zapp.app.mediathek.ui.detail.player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.ZappApplication
import de.christinecoenen.code.zapp.app.player.BackgroundPlayerService
import de.christinecoenen.code.zapp.app.player.BackgroundPlayerService.Companion.bind
import de.christinecoenen.code.zapp.app.player.Player
import de.christinecoenen.code.zapp.app.player.VideoInfo.Companion.fromShow
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.databinding.ActivityMediathekPlayerBinding
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.system.IntentHelper.openUrl
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper.isInPictureInPictureMode
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper.isInsideMultiWindow
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper.supportsPictureInPictureMode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class MediathekPlayerActivity : AppCompatActivity(), StyledPlayerControlView.VisibilityListener {

	companion object {
		private const val EXTRA_PERSISTED_SHOW_ID = "de.christinecoenen.code.zapp.EXTRA_PERSISTED_SHOW_ID"

		@JvmStatic
		fun getStartIntent(context: Context?, persistedShowId: Int): Intent {
			return Intent(context, MediathekPlayerActivity::class.java)
				.apply {
					action = Intent.ACTION_VIEW
					putExtra(EXTRA_PERSISTED_SHOW_ID, persistedShowId)
				}
		}
	}

	private lateinit var binding: ActivityMediathekPlayerBinding
	private lateinit var mediathekRepository: MediathekRepository
	private lateinit var settings: SettingsRepository

	private val pauseDisposables = CompositeDisposable()
	private var persistedShowId = 0
	private var persistedShow: PersistedMediathekShow? = null
	private var player: Player? = null
	private var binder: BackgroundPlayerService.Binder? = null

	private val backgroundPlayerServiceConnection: ServiceConnection = object : ServiceConnection {

		override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
			binder = service as BackgroundPlayerService.Binder
			binder!!.setForegroundActivityIntent(intent)

			player = binder!!.getPlayer()
			player!!.setView(binding.video)

			mediathekRepository.getPersistedShow(persistedShowId)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(::onShowLoaded, Timber::e)
				.also(pauseDisposables::add)
		}

		override fun onServiceDisconnected(componentName: ComponentName) {
			player?.pause()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMediathekPlayerBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(binding.toolbar)

		// set to show
		parseIntent(intent)

		settings = SettingsRepository(this)
		mediathekRepository = (applicationContext as ZappApplication).mediathekRepository

		binding.video.setControllerVisibilityListener(this)
		binding.video.requestFocus()
		binding.error.setOnClickListener(::onErrorViewClick)
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)

		// called when coming back from picture in picture mode
		parseIntent(intent)
	}

	private fun onErrorViewClick(view: View) {
		hideError()

		player?.recreate()
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

		requestedOrientation = if (settings.lockVideosInLandcapeFormat) {
			ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
		} else {
			ActivityInfo.SCREEN_ORIENTATION_SENSOR
		}
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

		binding.video.useController = !isInPictureInPictureMode
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.activity_mediathek_player, menu)

		if (!supportsPictureInPictureMode(this)) {
			menu.removeItem(R.id.menu_pip)
		}

		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_share -> {
				openUrl(this, persistedShow!!.mediathekShow.getVideoUrl(Quality.Medium))
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
		if (visibility == View.VISIBLE) {
			showSystemUi()
		} else {
			hideSystemUi()
		}
	}

	private fun onShowLoaded(persistedMediathekShow: PersistedMediathekShow) {
		persistedShow = persistedMediathekShow

		if (supportActionBar != null) {
			title = persistedMediathekShow.mediathekShow.topic
			supportActionBar!!.subtitle = persistedMediathekShow.mediathekShow.title
		}

		val videoInfo = fromShow(persistedMediathekShow)
		player!!.load(videoInfo)
		player!!.resume()

		player!!.errorResourceId
			.subscribe(::onVideoError, Timber::e)
			.also(pauseDisposables::add)

		binder!!.movePlaybackToForeground()

		val isInPipMode = isInPictureInPictureMode(this@MediathekPlayerActivity)
		onPictureInPictureModeChanged(isInPipMode, Configuration())
	}

	private fun onVideoError(messageResourceId: Int?) {
		if (messageResourceId == null || messageResourceId == -1) {
			hideError()
		} else {
			showError(messageResourceId)
		}
	}

	private fun pauseActivity() {
		pauseDisposables.clear()

		try {
			unbindService(backgroundPlayerServiceConnection)
		} catch (ignored: IllegalArgumentException) {
		}
	}

	private fun parseIntent(intent: Intent) {
		persistedShowId = intent.extras!!.getInt(EXTRA_PERSISTED_SHOW_ID, 0)

		if (persistedShowId == 0) {
			Toast.makeText(this, R.string.error_mediathek_called_without_show, Toast.LENGTH_LONG).show()
			finish()
			return
		}

		supportActionBar?.setDisplayHomeAsUpEnabled(true)
	}

	private fun resumeActivity() {
		hideError()
		bind(this, backgroundPlayerServiceConnection)
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
		binding.fullscreenContent.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	}

	private fun hideSystemUi() {
		supportActionBar?.hide()

		binding.fullscreenContent.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
			or View.SYSTEM_UI_FLAG_FULLSCREEN
			or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
	}
}
