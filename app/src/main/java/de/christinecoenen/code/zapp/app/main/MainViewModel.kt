package de.christinecoenen.code.zapp.app.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import de.christinecoenen.code.zapp.R


class MainViewModel(application: Application) : AndroidViewModel(application) {

	init {
		PreferenceManager.setDefaultValues(application, R.xml.preferences, false)
	}
}
