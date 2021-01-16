package de.christinecoenen.code.zapp.utils.view

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

object ColorHelper {

	@JvmStatic
	@ColorInt
	fun interpolate(percent: Float, @ColorInt color1: Int, @ColorInt color2: Int) =
		ColorUtils.blendARGB(color1, color2, percent)

	@JvmStatic
	@ColorInt
	fun darker(@ColorInt color: Int, amount: Float) =
		ColorUtils.blendARGB(color, Color.BLACK, amount)

	@JvmStatic
	@ColorInt
	fun withAlpha(@ColorInt color: Int, alpha: Int) =
		ColorUtils.setAlphaComponent(color, alpha)

}
