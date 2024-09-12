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
					WindowInsetsCompat.Type.ime()
			)
			v.updatePadding(bottom = systemBars.bottom)
			insets
		}
	}
}
