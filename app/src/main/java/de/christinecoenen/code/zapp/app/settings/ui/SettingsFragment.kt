package de.christinecoenen.code.zapp.app.settings.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.helper.ShortcutPreference
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import java.util.*

/**
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : PreferenceFragmentCompat() {

	companion object {

		private const val PREF_SHORTCUTS = "pref_shortcuts"
		private const val PREF_UI_MODE = "pref_ui_mode"

		/**
		 * Use this factory method to create a new instance of
		 * this fragment.
		 */
		@JvmStatic
		fun newInstance(): SettingsFragment {
			return SettingsFragment()
		}

	}

	private lateinit var settingsRepository: SettingsRepository
	private lateinit var shortcutPreference: ShortcutPreference
	private lateinit var uiModePreference: ListPreference

	private val uiModeChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
		val uiMode = settingsRepository.prefValueToUiMode(newValue as String?)
		AppCompatDelegate.setDefaultNightMode(uiMode)
		true
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)

		settingsRepository = SettingsRepository(context)
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		addPreferencesFromResource(R.xml.preferences)

		shortcutPreference = preferenceScreen.findPreference(PREF_SHORTCUTS)!!
		uiModePreference = preferenceScreen.findPreference(PREF_UI_MODE)!!
	}

	override fun onResume() {
		super.onResume()

		shortcutPreference.onPreferenceChangeListener = shortcutPreference
		uiModePreference.onPreferenceChangeListener = uiModeChangeListener
	}

	override fun onPause() {
		super.onPause()

		shortcutPreference.onPreferenceChangeListener = null
		uiModePreference.onPreferenceChangeListener = null
	}

	override fun onDisplayPreferenceDialog(preference: Preference) {

		if (preference is DeleteSearchQueriesPreference) {
			val dialogFragment = DeleteSearchQueriesPreferenceDialog.newInstance(preference.getKey())
			dialogFragment.setTargetFragment(this, 0)
			dialogFragment.show(Objects.requireNonNull(parentFragmentManager), null)
			return
		}

		super.onDisplayPreferenceDialog(preference)
	}
}
