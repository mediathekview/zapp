package de.christinecoenen.code.zapp.app.settings.ui

import android.os.Bundle
import android.view.View
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.databinding.DialogEditTextPreferenceBinding
import de.christinecoenen.code.zapp.utils.system.SystemUiHelper.applyBottomInsetAsPadding

abstract class BaseSettingsFragment : PreferenceFragmentCompat() {

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		listView.applyBottomInsetAsPadding()
	}

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

			is MultiSelectListPreference -> {
				val selected = preference.entryValues.map {
					preference.values.contains(it)
				}

				MaterialAlertDialogBuilder(requireContext())
					.setTitle(preference.title)
					.setMultiChoiceItems(
						preference.entries,
						selected.toBooleanArray()
					) { _, index, isChecked ->
						val newValue = preference.entryValues[index].toString()
						if (isChecked) {
							preference.values.add(newValue)
						} else {
							preference.values.remove(newValue)
						}
					}
					.setPositiveButton(android.R.string.ok) { _, _ ->
						preference.callChangeListener(preference.values)
					}
					.show()
			}

			else -> super.onDisplayPreferenceDialog(preference)
		}
	}
}
