package de.christinecoenen.code.zapp.utils.video;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;


public class SwipeablePlayerView extends PlayerView implements View.OnTouchListener, AspectRatioFrameLayout.AspectRatioListener {

	private static final int INDICATOR_WIDTH = 300;

	private PlayerControlView controlView;
	private SettingsRepository settingsRepository;
	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleGestureDetector;
	private SwipeIndicatorView volumeIndicator;
	private SwipeIndicatorView brightnessIndicator;
	private Window window;
	private AudioManager audioManager;
	private boolean hasAspectRatioMismatch = false;

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
		view.setOnTouchListener(this);
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

	public void showControls() {
		controlView.show();
	}

	@Override
	public boolean performClick() {
		if (getUseController()) {
			toggleControls();
		}
		return super.performClick();
	}

	@Override
	public void setPlayer(@Nullable Player player) {
		super.setPlayer(player);

		if (player != null) {
			player.addListener(new ScreenDimmingVideoEventListener(this));
		}
	}

	private void init(Context context) {
		controlView = (PlayerControlView) getChildAt(2);
		window = ((Activity) context).getWindow();
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		volumeIndicator = new SwipeIndicatorView(context);
		volumeIndicator.setIconResId(R.drawable.ic_volume_up_white_24dp);
		addView(volumeIndicator, new LayoutParams(INDICATOR_WIDTH, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END));

		brightnessIndicator = new SwipeIndicatorView(context);
		brightnessIndicator.setIconResId(R.drawable.ic_brightness_6_white_24dp);
		addView(brightnessIndicator, new LayoutParams(INDICATOR_WIDTH, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START));

		gestureDetector = new GestureDetector(context.getApplicationContext(), new WipingControlGestureListener());
		gestureDetector.setIsLongpressEnabled(false);

		scaleGestureDetector = new ScaleGestureDetector(context.getApplicationContext(), new ScaleGestureListener());

		setOnTouchListener(this);
		setAspectRatioListener(this);

		setLayoutTransition(new LayoutTransition());

		settingsRepository = new SettingsRepository(getContext());
		if (settingsRepository.getIsPlayerZoomed()) {
			setZoomStateCropped();
		} else {
			setZoomStateBoxed();
		}
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

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		gestureDetector.onTouchEvent(motionEvent);
		scaleGestureDetector.onTouchEvent(motionEvent);

		if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
			endScroll();
		}

		return getUseController();
	}

	private void setZoomStateCropped() {
		setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
		settingsRepository.setIsPlayerZoomed(true);
	}

	private void setZoomStateBoxed() {
		setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
		settingsRepository.setIsPlayerZoomed(false);
	}

	private boolean isZoomStateCropped() {
		return getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
	}

	private boolean isZoomStateBoxed() {
		return getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FIT;
	}

	@Override
	public void onAspectRatioUpdated(float targetAspectRatio, float naturalAspectRatio, boolean aspectRatioMismatch) {
		hasAspectRatioMismatch = aspectRatioMismatch;
	}

	private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			if (!hasAspectRatioMismatch) {
				return;
			}

			if (detector.getScaleFactor() > 1) {
				if (!isZoomStateCropped()) {
					setZoomStateCropped();
					Toast.makeText(getContext(), R.string.player_zoom_state_cropped, Toast.LENGTH_SHORT).show();
				}
			} else {
				if (!isZoomStateBoxed()) {
					setZoomStateBoxed();
					Toast.makeText(getContext(), R.string.player_zoom_state_boxed, Toast.LENGTH_SHORT).show();
				}
			}
		}

	}

	private class WipingControlGestureListener extends GestureDetector.SimpleOnGestureListener {

		private boolean canUseWipeControls = false;
		private float maxVerticalMovement;

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
				adjustBrightness(yPercent);
				return true;
			} else if (e2.getX() > getWidth() - INDICATOR_WIDTH) {
				adjustVolume(yPercent);
				return true;
			} else {
				endScroll();
				return super.onScroll(e1, e2, distanceX, distanceY);
			}
		}
	}
}
