package de.christinecoenen.code.zapp.utils.system

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding


object SystemUiHelper {

	/**
	 * Sets the bottom padding of this view to the bottom system bar inset to avoid overlapping
	 * with system ui.
	 */
	fun View.applyBottomInsetAsPadding() {
		ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
			val systemBars = insets.getInsets(
				WindowInsetsCompat.Type.systemBars() or
					WindowInsetsCompat.Type.ime() or
					WindowInsetsCompat.Type.displayCutout()
			)
			v.updatePadding(bottom = systemBars.bottom)
			insets
		}
	}

	/**
	 * Sets the horizontal padding of this view to the devices cutouts to avoid overlapping
	 * with them.
	 */
	fun View.applyHorizontalInsetsAsPadding() {
		ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
			val systemBars = insets.getInsets(
				WindowInsetsCompat.Type.systemBars() or
					WindowInsetsCompat.Type.ime() or
					WindowInsetsCompat.Type.displayCutout()
			)
			v.updatePadding(left = systemBars.left, right = systemBars.right)
			insets
		}
	}
}
