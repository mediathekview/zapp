package de.christinecoenen.code.zapp.app.about.ui

import android.view.View
import com.mikepenz.aboutlibraries.LibsConfiguration
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.SpecialButton

abstract class AbstractLibsListener : LibsConfiguration.LibsListener {
	override fun onExtraClicked(v: View, specialButton: SpecialButton) = false

	override fun onIconClicked(v: View) {}

	override fun onIconLongClicked(v: View) = false

	override fun onLibraryAuthorClicked(v: View, library: Library) = false

	override fun onLibraryAuthorLongClicked(v: View, library: Library) = false

	override fun onLibraryBottomClicked(v: View, library: Library) = false

	override fun onLibraryBottomLongClicked(v: View, library: Library) = false

	override fun onLibraryContentClicked(v: View, library: Library) = false

	override fun onLibraryContentLongClicked(v: View, library: Library) = false
}
