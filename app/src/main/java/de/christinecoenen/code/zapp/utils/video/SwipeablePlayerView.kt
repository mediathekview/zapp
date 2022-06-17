package de.christinecoenen.code.zapp.utils.video

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View.OnTouchListener
import android.widget.Toast
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.AspectRatioListener
import com.google.android.exoplayer2.ui.StyledPlayerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import kotlin.math.abs
import kotlin.math.max

class SwipeablePlayerView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null
) : StyledPlayerView(context, attrs), OnTouchListener, AspectRatioListener {

	companion object {
		private const val INDICATOR_WIDTH = 300
	}

	private var volumeIndicator = SwipeIndicatorView(context)
	private var brightnessIndicator = SwipeIndicatorView(context)

	private var window: Window = (context as Activity).window
	private var audioManager: AudioManager =
		context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

	private var settingsRepository: SettingsRepository
	private var gestureDetector: GestureDetector
	private var scaleGestureDetector: ScaleGestureDetector

	private var hasAspectRatioMismatch = false


	private val isZoomStateCropped: Boolean
		get() = resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM

	private val isZoomStateBoxed: Boolean
		get() = resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT


	init {

		volumeIndicator.setIconResId(R.drawable.ic_volume_up_white_24dp)
		addView(
			volumeIndicator,
			LayoutParams(INDICATOR_WIDTH, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END)
		)

		brightnessIndicator.setIconResId(R.drawable.ic_brightness_6_white_24dp)
		addView(
			brightnessIndicator,
			LayoutParams(INDICATOR_WIDTH, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START)
		)

		gestureDetector =
			GestureDetector(context.applicationContext, WipingControlGestureListener())
		gestureDetector.setIsLongpressEnabled(false)
		scaleGestureDetector =
			ScaleGestureDetector(context.applicationContext, ScaleGestureListener())

		setOnTouchListener(this)
		setAspectRatioListener(this)

		layoutTransition = LayoutTransition()

		settingsRepository = SettingsRepository(getContext())

		if (settingsRepository.isPlayerZoomed) {
			setZoomStateCropped()
		} else {
			setZoomStateBoxed()
		}
	}

	fun toggleControls() {
		if (isControllerFullyVisible) {
			hideController()
		} else {
			showController()
		}
	}

	private fun adjustBrightness(yPercent: Float) {
		val lp = window.attributes
		lp.screenBrightness = yPercent
		window.attributes = lp
		brightnessIndicator.setValue(yPercent)
	}

	private fun adjustVolume(yPercent: Float) {
		val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
		val volume = (yPercent * maxVolume).toInt()
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
		volumeIndicator.setValue(yPercent)
	}

	private fun endScroll() {
		volumeIndicator.visibility = GONE
		brightnessIndicator.visibility = GONE
	}

	override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
		gestureDetector.onTouchEvent(motionEvent)
		scaleGestureDetector.onTouchEvent(motionEvent)

		if (motionEvent.action == MotionEvent.ACTION_UP) {
			endScroll()
		}

		return useController
	}

	private fun setZoomStateCropped() {
		resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
		settingsRepository.isPlayerZoomed = true
	}

	private fun setZoomStateBoxed() {
		resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
		settingsRepository.isPlayerZoomed = false
	}

	override fun onAspectRatioUpdated(
		targetAspectRatio: Float,
		naturalAspectRatio: Float,
		aspectRatioMismatch: Boolean
	) {
		hasAspectRatioMismatch = aspectRatioMismatch
	}

	private inner class ScaleGestureListener : SimpleOnScaleGestureListener() {

		override fun onScaleEnd(detector: ScaleGestureDetector) {
			if (!hasAspectRatioMismatch) {
				return
			}

			if (detector.scaleFactor > 1) {
				if (!isZoomStateCropped) {
					setZoomStateCropped()
					Toast.makeText(context, R.string.player_zoom_state_cropped, Toast.LENGTH_SHORT)
						.show()
				}
			} else {
				if (!isZoomStateBoxed) {
					setZoomStateBoxed()
					Toast.makeText(context, R.string.player_zoom_state_boxed, Toast.LENGTH_SHORT)
						.show()
				}
			}
		}

	}

	private inner class WipingControlGestureListener : SimpleOnGestureListener() {

		private var canUseWipeControls = false
		private var maxVerticalMovement = 0f

		override fun onSingleTapUp(e: MotionEvent): Boolean {
			performClick()
			return true
		}

		override fun onDown(e: MotionEvent): Boolean {
			maxVerticalMovement = 0f
			canUseWipeControls = !isControllerFullyVisible

			return super.onDown(e)
		}

		override fun onScroll(
			e1: MotionEvent,
			e2: MotionEvent,
			distanceX: Float,
			distanceY: Float
		): Boolean {
			if (!canUseWipeControls) {
				return super.onScroll(e1, e2, distanceX, distanceY)
			}

			val distanceYSinceTouchbegin = e1.y - e2.y
			maxVerticalMovement = max(maxVerticalMovement, abs(distanceYSinceTouchbegin))

			val enoughVerticalMovement = maxVerticalMovement > 100

			if (!enoughVerticalMovement) {
				return super.onScroll(e1, e2, distanceX, distanceY)
			}

			val yPercent = 1 - e2.y / height

			return when {
				e2.x < INDICATOR_WIDTH -> {
					adjustBrightness(yPercent)
					true
				}
				e2.x > width - INDICATOR_WIDTH -> {
					adjustVolume(yPercent)
					true
				}
				else -> {
					endScroll()
					super.onScroll(e1, e2, distanceX, distanceY)
				}
			}
		}
	}
}
