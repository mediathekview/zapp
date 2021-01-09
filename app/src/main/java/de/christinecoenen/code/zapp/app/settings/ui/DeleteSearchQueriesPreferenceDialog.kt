package de.christinecoenen.code.zapp.app.settings.ui

import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat
import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekSearchSuggestionsProvider.Companion.deleteAllQueries

class DeleteSearchQueriesPreferenceDialog : PreferenceDialogFragmentCompat() {

	companion object {

		fun newInstance(key: String): DeleteSearchQueriesPreferenceDialog {
			return DeleteSearchQueriesPreferenceDialog().apply {
				arguments = Bundle().apply {
					putString(ARG_KEY, key)
				}
			}
		}

	}

	override fun onDialogClosed(positiveResult: Boolean) {
		if (positiveResult) {
			deleteAllQueries(requireContext())
		}
	}
}
