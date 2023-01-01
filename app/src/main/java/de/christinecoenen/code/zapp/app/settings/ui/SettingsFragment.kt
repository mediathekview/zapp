package de.christinecoenen.code.zapp.app.settings.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.color.DynamicColors
import com.google.android.material.snackbar.Snackbar
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.helper.ShortcutPreference
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.utils.system.LanguageHelper

class SettingsFragment : PreferenceFragmentCompat() {

	companion object {

		private const val PREF_SHORTCUTS = "pref_shortcuts"
		private const val PREF_DYNAMIC_COLORS = "dynamic_colors"
		private const val PREF_UI_MODE = "pref_ui_mode"
		private const val PREF_LANGUAGE = "pref_key_language"
		private const val PREF_CHANNEL_SELECTION = "pref_key_channel_selection"

	}

	private lateinit var settingsRepository: SettingsRepository
	private lateinit var shortcutPreference: ShortcutPreference
	private lateinit var dynamicColorsPreference: SwitchPreferenceCompat
	private lateinit var uiModePreference: ListPreference
	private lateinit var languagePreference: ListPreference
	private lateinit var channelSelectionPreference: Preference

	private val uiModeChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
		val uiMode = settingsRepository.prefValueToUiMode(newValue as String?)
		AppCompatDelegate.setDefaultNightMode(uiMode)
		true
	}

	private val languageChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
		val languageTag = newValue as String
		val appLocale = LocaleListCompat.forLanguageTags(languageTag)
		AppCompatDelegate.setApplicationLocales(appLocale)

		true
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)

		settingsRepository = SettingsRepository(context)
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		addPreferencesFromResource(R.xml.preferences)

		shortcutPreference = preferenceScreen.findPreference(PREF_SHORTCUTS)!!
		dynamicColorsPreference = preferenceScreen.findPreference(PREF_DYNAMIC_COLORS)!!
		uiModePreference = preferenceScreen.findPreference(PREF_UI_MODE)!!
		languagePreference = preferenceScreen.findPreference(PREF_LANGUAGE)!!
		channelSelectionPreference = preferenceScreen.findPreference(PREF_CHANNEL_SELECTION)!!

		val languages = LanguageHelper.getAvailableLanguages(requireContext())
		languagePreference.value = LanguageHelper.getCurrentLanguageTag()
		languagePreference.entries = languages.values.toTypedArray()
		languagePreference.entryValues = languages.keys.toTypedArray()

		// only show the preference for dynamic colors when available (Android 12 and up)<
		dynamicColorsPreference.isVisible = DynamicColors.isDynamicColorAvailable()
		dynamicColorsPreference.setOnPreferenceChangeListener { _, _ ->
			view?.let {
				Snackbar.make(it, R.string.requires_restart, Snackbar.LENGTH_SHORT).show()
			}
			true
		}

		channelSelectionPreference.setOnPreferenceClickListener {
			val direction =
				SettingsFragmentDirections.toChannelSelectionFragment()
			findNavController().navigate(direction)
			true
		}
	}

	override fun onResume() {
		super.onResume()

		shortcutPreference.onPreferenceChangeListener = shortcutPreference
		uiModePreference.onPreferenceChangeListener = uiModeChangeListener
		languagePreference.onPreferenceChangeListener = languageChangeListener
	}

	override fun onPause() {
		super.onPause()

		shortcutPreference.onPreferenceChangeListener = null
		uiModePreference.onPreferenceChangeListener = null
		languagePreference.onPreferenceChangeListener = null
	}
}
