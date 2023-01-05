package de.christinecoenen.code.zapp.utils.system

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import de.christinecoenen.code.zapp.app.settings.helper.ShortcutPreference
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository

class PreferenceFragmentHelper(
	private val preferenceFragment: PreferenceFragmentCompat,
	private val settingsRepository: SettingsRepository,
) : DefaultLifecycleObserver {

	companion object {

		private const val PREF_SHORTCUTS = "pref_shortcuts"
		private const val PREF_UI_MODE = "pref_ui_mode"
		private const val PREF_LANGUAGE = "pref_key_language"
		private const val PREF_CHANNEL_SELECTION = "pref_key_channel_selection"

	}

	private var shortcutPreference: ShortcutPreference? = null
	private var uiModePreference: ListPreference? = null
	private var languagePreference: ListPreference? = null
	private var channelSelectionPreference: Preference? = null

	private var channelSelectionClickListener: OnPreferenceClickListener? = null

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

	init {
		preferenceFragment.lifecycle.addObserver(this)
	}

	fun initPreferences(channelSelectionClickListener: OnPreferenceClickListener? = null) {
		val preferenceScreen = preferenceFragment.preferenceScreen

		shortcutPreference = preferenceScreen.findPreference(PREF_SHORTCUTS)
		uiModePreference = preferenceScreen.findPreference(PREF_UI_MODE)
		languagePreference = preferenceScreen.findPreference(PREF_LANGUAGE)
		channelSelectionPreference = preferenceScreen.findPreference(PREF_CHANNEL_SELECTION)

		languagePreference?.let {
			val languages =
				LanguageHelper.getAvailableLanguages(preferenceFragment.requireContext())
			it.value = LanguageHelper.getCurrentLanguageTag()
			it.entries = languages.values.toTypedArray()
			it.entryValues = languages.keys.toTypedArray()
		}

		this.channelSelectionClickListener = channelSelectionClickListener
	}

	fun destroy() {
		preferenceFragment.lifecycle.removeObserver(this)
	}

	override fun onResume(owner: LifecycleOwner) {
		super.onResume(owner)

		shortcutPreference?.onPreferenceChangeListener = shortcutPreference
		uiModePreference?.onPreferenceChangeListener = uiModeChangeListener
		languagePreference?.onPreferenceChangeListener = languageChangeListener
		channelSelectionPreference?.onPreferenceClickListener = channelSelectionClickListener
	}

	override fun onPause(owner: LifecycleOwner) {
		super.onPause(owner)

		shortcutPreference?.onPreferenceChangeListener = null
		uiModePreference?.onPreferenceChangeListener = null
		languagePreference?.onPreferenceChangeListener = null
		channelSelectionPreference?.onPreferenceClickListener = null
	}
}
