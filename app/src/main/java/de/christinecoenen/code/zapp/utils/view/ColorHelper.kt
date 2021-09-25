package de.christinecoenen.code.zapp.utils.view

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

object ColorHelper {

	@JvmStatic
	@ColorInt
	fun darker(@ColorInt color: Int, amount: Float) =
		ColorUtils.blendARGB(color, Color.BLACK, amount)

}
