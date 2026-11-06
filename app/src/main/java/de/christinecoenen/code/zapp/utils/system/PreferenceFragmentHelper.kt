package de.christinecoenen.code.zapp.utils.system

import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.color.DynamicColors
import com.jakewharton.processphoenix.ProcessPhoenix
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.ui.dialogs.ConfirmDeleteDownloadDialog
import de.christinecoenen.code.zapp.app.personal.series.CollectionUpdateScheduler
import de.christinecoenen.code.zapp.app.settings.helper.ShortcutPreference
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.app.settings.ui.ConfirmResetPlaybackPositionsDialog
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import java.util.Timer
import kotlin.concurrent.schedule

class PreferenceFragmentHelper(
	private val preferenceFragment: PreferenceFragmentCompat,
	private val settingsRepository: SettingsRepository,
	private val mediathekRepository: MediathekRepository,
) : DefaultLifecycleObserver {

	companion object {

		private const val PREF_SHORTCUTS = "pref_shortcuts"
		private const val PREF_DYNAMIC_COLORS = "dynamic_colors"
		private const val PREF_UI_MODE = "pref_ui_mode"
		private const val PREF_LANGUAGE = "pref_key_language"
		private const val PREF_CHANNEL_SELECTION = "pref_key_channel_selection"
		private const val PREF_DELETE_STARTED_SHOWS = "pref_key_delete_started_shows"
		private const val PREF_WATCHED_SHOW_MODE = "pref_key_watched_show_mode"
		private const val PREF_WATCHED_SHOW_FADE = "pref_key_watched_show_fade"
		private const val PREF_COLLECTION_CHECK_INTERVAL = "pref_key_collection_check_interval"
		private const val WATCHED_SHOW_MODE_HIDE = "hide"

	}

	private var shortcutPreference: ShortcutPreference? = null
	private var dynamicColorsPreference: SwitchPreferenceCompat? = null
	private var uiModePreference: ListPreference? = null
	private var languagePreference: ListPreference? = null
	private var channelSelectionPreference: Preference? = null
	private var deleteStartedShowsPreference: Preference? = null
	private var watchedShowModePreference: ListPreference? = null
	private var watchedShowFadePreference: SeekBarPreference? = null
	private var collectionCheckIntervalPreference: ListPreference? = null

	private var channelSelectionClickListener: OnPreferenceClickListener? = null

	private var deleteStartedShowsClickListener: OnPreferenceClickListener? =
		OnPreferenceClickListener {
			val dialog = ConfirmResetPlaybackPositionsDialog()

			preferenceFragment.setFragmentResultListener(ConfirmDeleteDownloadDialog.REQUEST_KEY_CONFIRMED) { _, _ ->
				preferenceFragment.launchOnCreated {
					mediathekRepository.resetAllPlaybackPositions()
					Toast.makeText(
						preferenceFragment.requireContext(),
						R.string.pref_dialog_confirm_reset_playback_position_success_toast,
						Toast.LENGTH_SHORT
					).show()
				}
			}

			dialog.show(preferenceFragment.parentFragmentManager, null)

			true
		}

	private val dynamicColorChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
		// Delay to wait for pereferences to be persisted (on very fast devices)
		// before killing the app - there is no listener that fires *after* persisting.
		Timer().schedule(200) {
			ProcessPhoenix.triggerRebirth(preferenceFragment.requireContext())
		}
		true
	}

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

	private val watchedShowModeChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
		updateWatchedShowFadePreferenceVisibility(newValue as String?)
		true
	}

	private val collectionCheckIntervalChangeListener =
		Preference.OnPreferenceChangeListener { _, newValue ->
			val intervalMinutes = (newValue as String).toIntOrNull() ?: 0
			CollectionUpdateScheduler.schedule(
				preferenceFragment.requireContext(),
				intervalMinutes
			)
			true
		}

	init {
		preferenceFragment.lifecycle.addObserver(this)
	}

	fun initPreferences(channelSelectionClickListener: OnPreferenceClickListener? = null) {
		val preferenceScreen = preferenceFragment.preferenceScreen

		shortcutPreference = preferenceScreen.findPreference(PREF_SHORTCUTS)
		dynamicColorsPreference = preferenceScreen.findPreference(PREF_DYNAMIC_COLORS)
		uiModePreference = preferenceScreen.findPreference(PREF_UI_MODE)
		languagePreference = preferenceScreen.findPreference(PREF_LANGUAGE)
		channelSelectionPreference = preferenceScreen.findPreference(PREF_CHANNEL_SELECTION)
		deleteStartedShowsPreference = preferenceScreen.findPreference(PREF_DELETE_STARTED_SHOWS)
		watchedShowModePreference = preferenceScreen.findPreference(PREF_WATCHED_SHOW_MODE)
		watchedShowFadePreference = preferenceScreen.findPreference(PREF_WATCHED_SHOW_FADE)
		collectionCheckIntervalPreference =
			preferenceScreen.findPreference(PREF_COLLECTION_CHECK_INTERVAL)

		languagePreference?.let {
			val languages =
				LanguageHelper.getAvailableLanguages(preferenceFragment.requireContext())
			it.value = LanguageHelper.getCurrentLanguageTag()
			it.entries = languages.values.toTypedArray()
			it.entryValues = languages.keys.toTypedArray()
		}

		// only show the preference for dynamic colors when available (Android 12 and up)
		dynamicColorsPreference?.let {
			it.isVisible = DynamicColors.isDynamicColorAvailable()
		}

		this.channelSelectionClickListener = channelSelectionClickListener

		updateWatchedShowFadePreferenceVisibility(watchedShowModePreference?.value)
	}

	fun destroy() {
		preferenceFragment.lifecycle.removeObserver(this)
	}

	override fun onStart(owner: LifecycleOwner) {
		super.onStart(owner)

		shortcutPreference?.onPreferenceChangeListener = shortcutPreference
		dynamicColorsPreference?.onPreferenceChangeListener = dynamicColorChangeListener
		uiModePreference?.onPreferenceChangeListener = uiModeChangeListener
		languagePreference?.onPreferenceChangeListener = languageChangeListener
		channelSelectionPreference?.onPreferenceClickListener = channelSelectionClickListener
		deleteStartedShowsPreference?.onPreferenceClickListener = deleteStartedShowsClickListener
		watchedShowModePreference?.onPreferenceChangeListener = watchedShowModeChangeListener
		collectionCheckIntervalPreference?.onPreferenceChangeListener =
			collectionCheckIntervalChangeListener
	}

	override fun onDestroy(owner: LifecycleOwner) {
		super.onDestroy(owner)

		shortcutPreference?.onPreferenceChangeListener = null
		dynamicColorsPreference?.onPreferenceChangeListener = null
		uiModePreference?.onPreferenceChangeListener = null
		languagePreference?.onPreferenceChangeListener = null
		channelSelectionPreference?.onPreferenceClickListener = null
		deleteStartedShowsPreference?.onPreferenceClickListener = null
		watchedShowModePreference?.onPreferenceChangeListener = null
		collectionCheckIntervalPreference?.onPreferenceChangeListener = null
	}

	private fun updateWatchedShowFadePreferenceVisibility(mode: String?) {
		watchedShowFadePreference?.isVisible = mode != WATCHED_SHOW_MODE_HIDE
	}
}
