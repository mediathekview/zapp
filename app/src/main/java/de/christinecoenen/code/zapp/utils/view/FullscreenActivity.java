package de.christinecoenen.code.zapp.utils.view;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import de.christinecoenen.code.zapp.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public abstract class FullscreenActivity extends AppCompatActivity {

	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #autoHideUiMillis} milliseconds.
	 */
	private boolean autoHideUi;

	/**
	 * If {@link #autoHideUi} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private int autoHideUiMillis;

	/**
	 * Some older devices needs a small delay between UI widget updates
	 * and a change of the status and navigation bar.
	 */
	private int uiAnimationDelay;

	protected View contentView;
	protected View controlsView;

	private final Handler hideHandler = new Handler();
	private final Runnable hidePart2Runnable = new Runnable() {
		@SuppressLint("InlinedApi")
		@Override
		public void run() {
			// Delayed removal of status and navigation bar

			// Note that some of these constants are new as of API 16 (Jelly Bean)
			// and API 19 (KitKat). It is safe to use them, as they are inlined
			// at compile-time and do nothing on earlier devices.
			contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	};
	private final Runnable showPart2Runnable = new Runnable() {
		@Override
		public void run() {
			// Delayed display of UI elements
			ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				actionBar.show();
			}
			controlsView.setVisibility(View.VISIBLE);
		}
	};

	private boolean isVisible;
	private final Runnable hideRunnable = this::hide;

	@Override
	public void setContentView(View rootView) {
		contentView = rootView.findViewById(R.id.fullscreen_content);
		contentView.setOnClickListener(this::onFullscreenContentClick);
		controlsView = rootView.findViewById(R.id.fullscreen_content_controls);

		autoHideUi = getResources().getBoolean(R.bool.activity_fullscreen_auto_hide_ui);
		autoHideUiMillis = getResources().getInteger(R.integer.activity_fullscreen_auto_hide_ui_millis);
		uiAnimationDelay = getResources().getInteger(R.integer.activity_fullscreen_ui_animation_delay);

		super.setContentView(rootView);
	}

	@Override
	protected void onResume() {
		super.onResume();

		isVisible = true;

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_DPAD_CENTER:
				toggle();
				return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	protected void delayHide() {
		if (autoHideUi) {
			delayedHide(autoHideUiMillis);
		}
	}

	private void toggle() {
		if (isVisible) {
			hide();
		} else {
			show();
		}
	}

	protected void hide() {
		// Hide UI first
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
		controlsView.setVisibility(View.GONE);
		isVisible = false;

		// Schedule a runnable to remove the status and navigation bar after a delay
		hideHandler.removeCallbacks(showPart2Runnable);
		hideHandler.postDelayed(hidePart2Runnable, uiAnimationDelay);
	}

	@SuppressWarnings("WeakerAccess")
	@SuppressLint("InlinedApi")
	protected void show() {
		// Show the system bar
		contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		isVisible = true;

		// Schedule a runnable to display UI elements after a delay
		hideHandler.removeCallbacks(hidePart2Runnable);
		hideHandler.postDelayed(showPart2Runnable, uiAnimationDelay);
		delayHide();
	}

	private void onFullscreenContentClick(View view) {
		// Set up the user interaction to manually show or hide the system UI.
		toggle();
	}

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		hideHandler.removeCallbacks(hideRunnable);
		hideHandler.postDelayed(hideRunnable, delayMillis);
	}
}
