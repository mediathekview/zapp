package de.christinecoenen.code.zapp.utils.system

import android.app.Activity
import android.app.PictureInPictureParams
import androidx.appcompat.app.AppCompatActivity

object MultiWindowHelper {
	/**
	 * This function can be used with any API level and will return
	 * true if the activity is currently in pip mode.
	 *
	 * @param activity to get access to pip api
	 * @return true if activity is currently displayed in picture in picture mode
	 */
	@JvmStatic
	fun isInPictureInPictureMode(activity: Activity): Boolean {
		return activity.isInPictureInPictureMode
	}

	@JvmStatic
	fun enterPictureInPictureMode(activity: AppCompatActivity, params: PictureInPictureParams? = null) {
		val nullCheckedParams = params ?: PictureInPictureParams.Builder().build()
		activity.enterPictureInPictureMode(nullCheckedParams)
	}
}
