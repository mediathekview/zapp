package de.christinecoenen.code.zapp.utils.system

import android.view.Window
import androidx.core.view.WindowCompat


object SystemUiHelper {

	/**
	 * Can set light status bar (with dark text) or dark status bar (with light tetxt)
	 * programmatically - overriding the current app theme.
	 */
	fun useLightStatusBar(window: Window, lightStatusBar: Boolean) {
		val windowInsetController = WindowCompat.getInsetsController(window, window.decorView)
		windowInsetController.isAppearanceLightStatusBars = lightStatusBar
	}

}
