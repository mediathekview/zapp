package de.christinecoenen.code.zapp.app.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import de.christinecoenen.code.zapp.R


class MainViewModel(application: Application) : AndroidViewModel(application) {

	val pageCount get() = PageType.values().size

	init {
		PreferenceManager.setDefaultValues(application, R.xml.preferences, false)
	}

	fun getPageTypeAt(position: Int) = PageType.values()[position]

	fun getPageTypeFromMenuResId(itemId: Int) =
		when (itemId) {
			R.id.menu_live -> PageType.PAGE_CHANNEL_LIST
			R.id.menu_mediathek -> PageType.PAGE_MEDIATHEK_LIST
			R.id.menu_downloads -> PageType.PAGE_DOWNLOADS
			else -> throw IllegalArgumentException("Unknown menu item $itemId.")
		}
}
