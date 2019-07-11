package de.christinecoenen.code.zapp.app.settings.ui;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.settings.helper.ShortcutPreference;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;

/**
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
class SettingsFragment extends PreferenceFragmentCompat {

	private static final String PREF_SHORTCUTS = "pref_shortcuts";
	private static final String PREF_UI_MODE = "pref_ui_mode";

	/**
	 * Use this factory method to create a new instance of
	 * this fragment.
	 */
	static SettingsFragment newInstance() {
		return new SettingsFragment();
	}

	private SettingsRepository settingsRepository;
	private ShortcutPreference shortcutPreference;
	private ListPreference uiModePreference;

	private final Preference.OnPreferenceChangeListener uiModeChangeListener = (preference, newValue) -> {
		int uiMode = settingsRepository.prefValueToUiMode((String) newValue);
		AppCompatDelegate.setDefaultNightMode(uiMode);
		return true;
	};

	public SettingsFragment() {
		// required empty public constructor
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		settingsRepository = new SettingsRepository(context);
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.preferences);

		shortcutPreference = (ShortcutPreference) getPreferenceScreen()
			.findPreference(PREF_SHORTCUTS);
		uiModePreference = (ListPreference) getPreferenceScreen()
			.findPreference(PREF_UI_MODE);
	}

	@Override
	public void onResume() {
		super.onResume();
		shortcutPreference.setOnPreferenceChangeListener(shortcutPreference);
		uiModePreference.setOnPreferenceChangeListener(uiModeChangeListener);
	}

	@Override
	public void onPause() {
		super.onPause();
		shortcutPreference.setOnPreferenceChangeListener(null);
		uiModePreference.setOnPreferenceChangeListener(uiModeChangeListener);
	}
}
