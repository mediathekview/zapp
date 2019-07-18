package de.christinecoenen.code.zapp.utils.system;

import android.content.Context;
import android.content.res.Configuration;

public class ConfigurationHelper {

	public static boolean isInDarkMode(Context context) {
		int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
		return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
	}

}
