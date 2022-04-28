package de.christinecoenen.code.zapp.tv.about

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.christinecoenen.code.zapp.utils.system.IStartableActivity

data class AboutItem(
	@StringRes val titleResId: Int,
	@DrawableRes val iconResId: Int,
	val startableActivity: IStartableActivity
) {
	fun getTitle(context: Context) = context.getString(titleResId)
}
