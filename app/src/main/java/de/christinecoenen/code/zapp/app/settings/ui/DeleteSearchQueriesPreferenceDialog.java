package de.christinecoenen.code.zapp.app.settings.ui;

import android.os.Bundle;

import androidx.preference.PreferenceDialogFragmentCompat;

import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekSearchSuggestionsProvider;


public class DeleteSearchQueriesPreferenceDialog extends PreferenceDialogFragmentCompat {

	static DeleteSearchQueriesPreferenceDialog newInstance(String key) {
		final DeleteSearchQueriesPreferenceDialog fragment = new DeleteSearchQueriesPreferenceDialog();
		final Bundle b = new Bundle(1);
		b.putString(ARG_KEY, key);
		fragment.setArguments(b);

		return fragment;
	}

	@Override
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			MediathekSearchSuggestionsProvider.deleteAllQueries(getContext());
		}
	}

}
