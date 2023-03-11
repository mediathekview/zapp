package de.christinecoenen.code.zapp.app.settings.ui

import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.databinding.DialogEditTextPreferenceBinding

abstract class BaseSettingsFragment : PreferenceFragmentCompat() {
	/**
	 * Show [ListPreference] and [EditTextPreference] dialog by [MaterialAlertDialogBuilder]
	 */
	override fun onDisplayPreferenceDialog(preference: Preference) {
		when (preference) {
			is ListPreference -> {
				val prefIndex = preference.entryValues.indexOf(preference.value)
				MaterialAlertDialogBuilder(requireContext())
					.setTitle(preference.title)
					.setSingleChoiceItems(preference.entries, prefIndex) { dialog, index ->
						val newValue = preference.entryValues[index].toString()
						if (preference.callChangeListener(newValue)) {
							preference.value = newValue
						}
						dialog.dismiss()
					}
					.setNegativeButton(android.R.string.cancel, null)
					.show()
			}
			is EditTextPreference -> {
				val binding = DialogEditTextPreferenceBinding.inflate(layoutInflater)
				val preferences = SettingsRepository(requireContext()).preferences
				binding.editText.setText(preferences.getString(preference.key, ""))
				MaterialAlertDialogBuilder(requireContext())
					.setTitle(preference.title)
					.setView(binding.root)
					.setPositiveButton(android.R.string.ok) { _, _ ->
						val newValue = binding.editText.text.toString()
						if (preference.callChangeListener(newValue)) {
							preference.text = newValue
						}
					}
					.setNegativeButton(android.R.string.cancel, null)
					.show()
			}
			else -> super.onDisplayPreferenceDialog(preference)
		}
	}
}
