package de.christinecoenen.code.zapp.tv.settings

import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen


class SettingsFragment : LeanbackSettingsFragmentCompat() {

	override fun onPreferenceStartFragment(
		caller: PreferenceFragmentCompat,
		pref: Preference
	): Boolean {
		return false
	}

	override fun onPreferenceStartScreen(
		caller: PreferenceFragmentCompat,
		pref: PreferenceScreen
	): Boolean {
		return false
	}

	override fun onPreferenceStartInitialScreen() {
		startPreferenceFragment(PreferenceFragment())
	}

}
