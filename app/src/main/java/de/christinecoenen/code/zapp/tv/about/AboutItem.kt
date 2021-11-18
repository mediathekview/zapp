package de.christinecoenen.code.zapp.tv.about

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class AboutItem(
	@StringRes val titleResId: Int,
	@DrawableRes val iconResId: Int,
) {
	fun getTitle(context: Context) = context.getString(titleResId)
}
