package de.christinecoenen.code.zapp.app.settings.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.christinecoenen.code.zapp.R;

public class SettingsRepository {

	private final Context context;
	private final SharedPreferences preferences;

	public SettingsRepository(Context context) {
		this.context = context.getApplicationContext();

		preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
	}

	public boolean getLockVideosInLandcapeFormat() {
		return preferences.getBoolean(context.getString(R.string.pref_key_detail_landscape), true);
	}

	public boolean getWifiOnly() {
		return preferences.getBoolean(context.getString(R.string.pref_key_wifi_only), true);
	}

	public boolean getEnableSubtitles() {
		return preferences.getBoolean(context.getString(R.string.pref_key_enable_subtitles), false);
	}

	public void setEnableSubtitles(boolean enabled) {
		preferences.edit()
			.putBoolean(context.getString(R.string.pref_key_enable_subtitles), enabled)
			.apply();
	}
}
