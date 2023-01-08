package de.christinecoenen.code.zapp.app.settings.repository

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import de.christinecoenen.code.zapp.R
import java.util.*

class SettingsRepository(context: Context) {

	private val context = context.applicationContext
	private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

	val lockVideosInLandcapeFormat: Boolean
		get() = preferences.getBoolean(context.getString(R.string.pref_key_detail_landscape), true)

	val pictureInPictureOnBack: Boolean
		get() = preferences.getBoolean(context.getString(R.string.pref_key_pip_on_back), false)

	val meteredNetworkStreamQuality: StreamQualityBucket
		@SuppressLint("DefaultLocale")
		get() = preferences.getString(context.getString(R.string.pref_key_stream_quality_over_metered_network), null).let { quality ->
			if (quality == null) {
				StreamQualityBucket.DISABLED
			} else {
				StreamQualityBucket.valueOf(quality.uppercase(Locale.ENGLISH))
			}
		}

	val downloadOverUnmeteredNetworkOnly: Boolean
		get() = preferences.getBoolean(context.getString(R.string.pref_key_download_over_unmetered_network_only), true)

	var isPlayerZoomed: Boolean
		get() = preferences.getBoolean(context.getString(R.string.pref_key_player_zoomed), false)
		set(enabled) {
			preferences.edit()
				.putBoolean(context.getString(R.string.pref_key_player_zoomed), enabled)
				.apply()
		}

	val downloadToSdCard: Boolean
		get() = preferences.getBoolean(context.getString(R.string.pref_key_download_to_sd_card), true)

	val dynamicColors: Boolean
		get() = preferences.getBoolean(context.getString(R.string.pref_key_dynamic_colors), false)

	val uiMode: Int
		get() {
			val uiMode = preferences.getString(context.getString(R.string.pref_key_ui_mode), null)
			return prefValueToUiMode(uiMode)
		}

	fun prefValueToUiMode(prefSetting: String?): Int {
		val defaultMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
			AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY

		return when (prefSetting) {
			"light" ->
				AppCompatDelegate.MODE_NIGHT_NO
			"dark" ->
				AppCompatDelegate.MODE_NIGHT_YES
			else ->
				defaultMode
		}
	}
}
