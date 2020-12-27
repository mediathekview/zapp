package de.christinecoenen.code.zapp.utils.system

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object MultiWindowHelper {
	/**
	 * This function can be used with any API level and will return
	 * false if the multi window feature is not supported.
	 *
	 * @param activity to get access to multi window api
	 * @return true if activity is currently displayed in multi window mode
	 */
	@JvmStatic
	@TargetApi(24)
	fun isInsideMultiWindow(activity: Activity): Boolean {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
			(activity.isInMultiWindowMode || activity.isInPictureInPictureMode)
	}

	/**
	 * This function can be used with any API level and will return
	 * true if the activity is currently in pip mode.
	 *
	 * @param activity to get access to pip api
	 * @return true if activity is currently displayed in picture in picture mode
	 */
	@JvmStatic
	@TargetApi(24)
	fun isInPictureInPictureMode(activity: Activity): Boolean {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInPictureInPictureMode
	}

	/**
	 * This function can be used with any API level and will return
	 * false if the picture in picture feature is not supported.
	 *
	 * @return true if the current device does support picture in picture mode
	 */
	@JvmStatic
	fun supportsPictureInPictureMode(context: Context): Boolean {
		val packageManager = context.applicationContext.packageManager
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
			packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
	}
}
