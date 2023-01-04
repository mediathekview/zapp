package de.christinecoenen.code.zapp.tv.settings

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import de.christinecoenen.code.zapp.R

class PreferenceFragment : LeanbackPreferenceFragmentCompat() {

	// TODO: initialize settings and add listeners

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.tv_preferences, rootKey)
	}

}
