package de.christinecoenen.code.zapp.utils.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

import timber.log.Timber;


public class SwipeablePlayerView extends PlayerView {

	private PlayerControlView controlView;
	private GestureDetector gestureDetector;
	private WipingControlGestureListener listener;
	private Window window;
	private AudioManager audioManager;

	public SwipeablePlayerView(Context context) {
		super(context);
		init(context);
	}

	public SwipeablePlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SwipeablePlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public void setTouchOverlay(View view) {
		view.setOnTouchListener(listener);
	}

	public void toggleControls() {
		if (controlView.isVisible()) {
			controlView.hide();
		} else {
			controlView.show();
		}
	}

	public void hideControls() {
		controlView.hide();
	}

	@Override
	public boolean performClick() {
		if (getUseController()) {
			toggleControls();
		}
		return super.performClick();
	}

	private void init(Context context) {
		controlView = (PlayerControlView) getChildAt(2);
		window = ((Activity) context).getWindow();
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		listener = new WipingControlGestureListener();
		gestureDetector = new GestureDetector(context.getApplicationContext(), listener);
		gestureDetector.setIsLongpressEnabled(false);
		setOnTouchListener(listener);
	}

	private void adjustBrightness(float yPercent) {
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.screenBrightness = yPercent;
		window.setAttributes(lp);

		Timber.v("adjustBrightness: %f", yPercent);
	}

	private void adjustVolume(float yPercent) {
		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int volume = (int) (yPercent * maxVolume);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
	}

	private class WipingControlGestureListener extends GestureDetector.SimpleOnGestureListener implements OnTouchListener {

		private boolean canUseWipeControls = false;
		private float maxVerticalMovement;

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			gestureDetector.onTouchEvent(motionEvent);
			return getUseController();
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			performClick();
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			maxVerticalMovement = 0;
			canUseWipeControls = !controlView.isVisible();
			return super.onDown(e);
		}

		// TODO: show indicators / toasts
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (!canUseWipeControls || e1 == null) {
				return super.onScroll(e1, e2, distanceX, distanceY);
			}

			float distanceXSinceTouchbegin = e1.getX() - e2.getX();
			float distanceYSinceTouchbegin = e1.getY() - e2.getY();
			maxVerticalMovement = Math.max(maxVerticalMovement, Math.abs(distanceYSinceTouchbegin));
			boolean tooMuchSidewayMovement = Math.abs(distanceXSinceTouchbegin) > 100;
			boolean enoughVerticalMovement = maxVerticalMovement > 100;

			if (tooMuchSidewayMovement || !enoughVerticalMovement) {
				return super.onScroll(e1, e2, distanceX, distanceY);
			}

			float yPercent = 1 - (e2.getY() / getHeight());

			if (e2.getX() > getWidth() / 2) {
				adjustBrightness(yPercent);
			} else {
				adjustVolume(yPercent);
			}

			return true;
		}
	}
}
