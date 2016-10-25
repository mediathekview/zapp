package de.christinecoenen.code.zapp.fragments;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.christinecoenen.code.zapp.R;

/**
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends PreferenceFragment {

	/**
	 * Use this factory method to create a new instance of
	 * this fragment.
	 */
	public static SettingsFragment newInstance() {
		return new SettingsFragment();
	}


	public SettingsFragment() {
		// required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
