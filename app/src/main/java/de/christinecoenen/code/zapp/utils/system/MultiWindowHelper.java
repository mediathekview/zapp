package de.christinecoenen.code.zapp.utils.system;


import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

public class MultiWindowHelper {

	/**
	 * This function can be used with any API level and will return
	 * false if the multi window feature is not supported.
	 * @param activity to get access to multi window api
	 * @return true if activity is currently displayed in multi window mode
     */
	@TargetApi(24)
	public static boolean isInsideMultiWindow(Activity activity) {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode();
	}

}
