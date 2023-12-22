package de.christinecoenen.code.zapp.app.mediathek.ui.helper

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnResumed
import kotlinx.coroutines.flow.collectLatest

class ShowMenuProvider(
	private val fragment: Fragment,
	show: MediathekShow
) : MenuProvider {

	private val showMenuHelper = ShowMenuHelper(fragment, show)

	init {
		fragment.launchOnResumed {
			showMenuHelper.invalidateOptionsMenuFlow.collectLatest {
				fragment.requireActivity().invalidateOptionsMenu()
			}
		}
	}

	override fun onPrepareMenu(menu: Menu) {
		showMenuHelper.prepareMenu(menu)
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		showMenuHelper.inflateShowMenu(menu, menuInflater)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return showMenuHelper.onMenuItemSelected(menuItem)
	}
}
