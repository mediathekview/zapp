package de.christinecoenen.code.zapp.app.mediathek.ui.helper

import android.view.*
import android.widget.PopupMenu
import android.widget.PopupMenu.OnMenuItemClickListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.ui.dialogs.ConfirmDeleteDownloadDialog
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Helper class to display and manage (context) menus related to mediathek shows.
 * The current state of the show will be automatically applied to the menu.
 *
 * For toolbar option menus use use ShowMenuProvider.
 */
class ShowMenuHelper(
	private val fragment: Fragment,
	private val show: MediathekShow
) : OnMenuItemClickListener {

	private val viewModel: ShowMenuHelperViewModel by fragment.viewModel()

	private var updateMenuItemsJob: Job? = null

	val invalidateOptionsMenuFlow = viewModel
		.getMenuItemsVisibility(show)

	fun showContextMenu(view: View, listener: OnMenuItemClickListener = this) {
		PopupMenu(fragment.requireContext(), view, Gravity.TOP or Gravity.END).apply {
			inflateShowMenu(menu, menuInflater)
			runBlocking {
				// we need to call this blocking to avoid menu flicker on open
				prepareMenuAsync(menu)
			}

			show()

			setOnMenuItemClickListener(listener)
			setOnDismissListener { updateMenuItemsJob?.cancel() }

			startUpdateMenuJob(menu)
		}
	}

	fun inflateShowMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.mediathek_show, menu)
	}

	fun prepareMenu(menu: Menu) {
		fragment.lifecycleScope.launchWhenResumed {
			prepareMenuAsync(menu)
		}
	}

	suspend fun prepareMenuAsync(menu: Menu) {
		viewModel
			.getMenuItemsVisibility(show)
			.first()
			.let {
				applyVisibiltyToMenu(menu, it)
			}
	}

	fun onMenuItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_share -> {
				show.shareExternally(fragment.requireContext())
				true
			}
			R.id.menu_remove_download -> {
				showConfirmDeleteDownloadDialog()
				return true
			}
			R.id.menu_cancel_download -> {
				fragment.lifecycleScope.launchWhenResumed {
					viewModel.cancelDownload(show)
				}
				return true
			}
			R.id.menu_mark_unwatched -> {
				fragment.lifecycleScope.launchWhenResumed {
					viewModel.markUnwatched(show)
				}
				return true
			}
			else -> false
		}
	}

	override fun onMenuItemClick(item: MenuItem?) =
		if (item == null) false else onMenuItemSelected(item)

	private fun startUpdateMenuJob(menu: Menu) {
		updateMenuItemsJob?.cancel()

		updateMenuItemsJob = fragment.lifecycleScope.launchWhenResumed {
			viewModel
				.getMenuItemsVisibility(show)
				.collectLatest { applyVisibiltyToMenu(menu, it) }
		}
	}

	private fun applyVisibiltyToMenu(menu: Menu, menuVisibilityMap: Map<Int, Boolean>) {
		menuVisibilityMap.forEach {
			menu.findItem(it.key).isVisible = it.value
		}
	}

	private fun showConfirmDeleteDownloadDialog() {
		val dialog = ConfirmDeleteDownloadDialog()

		fragment.setFragmentResultListener(ConfirmDeleteDownloadDialog.REQUEST_KEY_CONFIRMED) { _, _ ->
			fragment.viewLifecycleOwner.lifecycleScope.launch {
				viewModel.deleteDownload(show)
			}
		}

		dialog.show(fragment.parentFragmentManager, null)
	}
}
