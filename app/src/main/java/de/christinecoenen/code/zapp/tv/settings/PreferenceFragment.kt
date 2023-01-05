package de.christinecoenen.code.zapp.tv.settings

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.utils.system.PreferenceFragmentHelper
import org.koin.android.ext.android.inject

class PreferenceFragment : LeanbackPreferenceFragmentCompat() {

	private val settingsRepository: SettingsRepository by inject()
	private val preferenceFragmentHelper = PreferenceFragmentHelper(this, settingsRepository)

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.tv_preferences, rootKey)

		preferenceFragmentHelper.initPreferences()
	}

	override fun onDestroy() {
		super.onDestroy()
		preferenceFragmentHelper.destroy()
	}
}
