package de.christinecoenen.code.zapp.app.settings.ui

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.PreferenceFragmentHelper
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class SettingsFragment : BaseSettingsFragment() {

	private val preferenceFragmentHelper: PreferenceFragmentHelper by inject { parametersOf(this) }

	private val channelSelectionClickListener = Preference.OnPreferenceClickListener {
		val direction =
			SettingsFragmentDirections.toChannelSelectionFragment()
		findNavController().navigate(direction)
		true
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		addPreferencesFromResource(R.xml.preferences)

		preferenceFragmentHelper.initPreferences(channelSelectionClickListener)
	}

	override fun onDestroy() {
		super.onDestroy()
		preferenceFragmentHelper.destroy()
	}
}
