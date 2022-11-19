package de.christinecoenen.code.zapp.app.mediathek.ui.list.helper

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShowMenuProvider(
	private val fragment: Fragment,
	show: MediathekShow
) : MenuProvider {

	private val showMenuHelper = ShowMenuHelper(fragment, show)

	init {
		fragment.lifecycleScope.launch {
			fragment.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
				showMenuHelper.invalidateOptionsMenuFlow.collectLatest {
					fragment.requireActivity().invalidateOptionsMenu()
				}
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
