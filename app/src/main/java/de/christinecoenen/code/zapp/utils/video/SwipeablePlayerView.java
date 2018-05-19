package de.christinecoenen.code.zapp.utils.video;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

import de.christinecoenen.code.zapp.R;


public class SwipeablePlayerView extends PlayerView {

	private static final int INDICATOR_WIDTH = 300;

	private PlayerControlView controlView;
	private GestureDetector gestureDetector;
	private SwipeIndicatorView volumeIndicator;
	private SwipeIndicatorView brightnessIndicator;
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

		volumeIndicator = new SwipeIndicatorView(context);
		volumeIndicator.setIconResId(R.drawable.ic_volume_up_white_24dp);
		addView(volumeIndicator, new LayoutParams(INDICATOR_WIDTH, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START));

		brightnessIndicator = new SwipeIndicatorView(context);
		brightnessIndicator.setIconResId(R.drawable.ic_brightness_6_white_24dp);
		addView(brightnessIndicator, new LayoutParams(INDICATOR_WIDTH, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END));

		listener = new WipingControlGestureListener();
		gestureDetector = new GestureDetector(context.getApplicationContext(), listener);
		gestureDetector.setIsLongpressEnabled(false);
		setOnTouchListener(listener);

		setLayoutTransition(new LayoutTransition());
	}

	private void adjustBrightness(float yPercent) {
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.screenBrightness = yPercent;
		window.setAttributes(lp);

		brightnessIndicator.setValue(yPercent);
	}

	private void adjustVolume(float yPercent) {
		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int volume = (int) (yPercent * maxVolume);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

		volumeIndicator.setValue(yPercent);
	}

	private void endScroll() {
		volumeIndicator.setVisibility(GONE);
		brightnessIndicator.setVisibility(GONE);
	}

	private class WipingControlGestureListener extends GestureDetector.SimpleOnGestureListener implements OnTouchListener {

		private boolean canUseWipeControls = false;
		private float maxVerticalMovement;

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			gestureDetector.onTouchEvent(motionEvent);

			switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_UP:
					endScroll();
					break;
			}

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

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (!canUseWipeControls || e1 == null) {
				return super.onScroll(e1, e2, distanceX, distanceY);
			}

			float distanceYSinceTouchbegin = e1.getY() - e2.getY();
			maxVerticalMovement = Math.max(maxVerticalMovement, Math.abs(distanceYSinceTouchbegin));
			boolean enoughVerticalMovement = maxVerticalMovement > 100;

			if (!enoughVerticalMovement) {
				return super.onScroll(e1, e2, distanceX, distanceY);
			}

			float yPercent = 1 - (e2.getY() / getHeight());

			if (e2.getX() < INDICATOR_WIDTH) {
				adjustVolume(yPercent);
				return true;
			} else if (e2.getX() > getWidth() - INDICATOR_WIDTH) {
				adjustBrightness(yPercent);
				return true;
			} else {
				endScroll();
				return super.onScroll(e1, e2, distanceX, distanceY);
			}
		}
	}
}
