package de.christinecoenen.code.zapp.tv.settings

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.PreferenceFragmentHelper
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class PreferenceFragment : LeanbackPreferenceFragmentCompat() {

	private val preferenceFragmentHelper: PreferenceFragmentHelper by inject { parametersOf(this) }

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.tv_preferences, rootKey)

		preferenceFragmentHelper.initPreferences()
	}

	override fun onDestroy() {
		super.onDestroy()
		preferenceFragmentHelper.destroy()
	}
}
