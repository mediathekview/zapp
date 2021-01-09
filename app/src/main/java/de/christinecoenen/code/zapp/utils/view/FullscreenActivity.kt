package de.christinecoenen.code.zapp.utils.view

import android.annotation.SuppressLint
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import de.christinecoenen.code.zapp.R

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
abstract class FullscreenActivity : AppCompatActivity() {

	/**
	 * Whether or not the system UI should be auto-hidden after
	 * [.autoHideUiMillis] milliseconds.
	 */
	private val autoHideUi by lazy { resources.getBoolean(R.bool.activity_fullscreen_auto_hide_ui) }

	/**
	 * If [.autoHideUi] is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private val autoHideUiMillis by lazy { resources.getInteger(R.integer.activity_fullscreen_auto_hide_ui_millis) }

	/**
	 * Some older devices needs a small delay between UI widget updates
	 * and a change of the status and navigation bar.
	 */
	private val uiAnimationDelay by lazy { resources.getInteger(R.integer.activity_fullscreen_ui_animation_delay) }

	protected lateinit var fullscreenContent: View
	protected lateinit var controlsView: View

	private var isUiShown = false
	private val hideHandler = Handler()

	private val hidePart2Runnable = Runnable { // Delayed removal of status and navigation bar

		// Note that some of these constants are new as of API 16 (Jelly Bean)
		// and API 19 (KitKat). It is safe to use them, as they are inlined
		// at compile-time and do nothing on earlier devices.
		fullscreenContent.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
			or View.SYSTEM_UI_FLAG_FULLSCREEN
			or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
	}

	private val showPart2Runnable = Runnable { // Delayed display of UI elements
		supportActionBar?.show()
		controlsView.visibility = View.VISIBLE
	}

	private val hideRunnable = Runnable(::hide)

	override fun setContentView(rootView: View) {
		fullscreenContent = rootView.findViewById(R.id.fullscreen_content)
		fullscreenContent.setOnClickListener(View.OnClickListener(::onFullscreenContentClick))

		controlsView = rootView.findViewById(R.id.fullscreen_content_controls)

		super.setContentView(rootView)
	}

	override fun onResume() {
		super.onResume()

		isUiShown = true

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100)
	}

	override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
		when (keyCode) {
			KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
				toggle()
				return true
			}
		}
		return super.onKeyUp(keyCode, event)
	}

	protected fun delayHide() {
		if (autoHideUi) {
			delayedHide(autoHideUiMillis)
		}
	}

	private fun toggle() {
		if (isUiShown) {
			hide()
		} else {
			show()
		}
	}

	protected fun hide() {
		// Hide UI first
		supportActionBar?.hide()
		controlsView.visibility = View.GONE
		isUiShown = false

		// Schedule a runnable to remove the status and navigation bar after a delay
		hideHandler.removeCallbacks(showPart2Runnable)
		hideHandler.postDelayed(hidePart2Runnable, uiAnimationDelay.toLong())
	}

	@SuppressLint("InlinedApi")
	protected fun show() {
		// Show the system bar
		fullscreenContent.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

		isUiShown = true

		// Schedule a runnable to display UI elements after a delay
		hideHandler.removeCallbacks(hidePart2Runnable)
		hideHandler.postDelayed(showPart2Runnable, uiAnimationDelay.toLong())

		delayHide()
	}

	private fun onFullscreenContentClick(view: View) {
		// Set up the user interaction to manually show or hide the system UI.
		toggle()
	}

	/**
	 * Schedules a call to hide() in [delayMillis] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private fun delayedHide(delayMillis: Int) {
		hideHandler.removeCallbacks(hideRunnable)
		hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
	}
}
