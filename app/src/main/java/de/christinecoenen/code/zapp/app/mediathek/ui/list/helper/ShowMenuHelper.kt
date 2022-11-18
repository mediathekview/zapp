package de.christinecoenen.code.zapp.app.mediathek.ui.list.helper

import android.view.*
import android.widget.PopupMenu
import android.widget.PopupMenu.OnMenuItemClickListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.downloads.ui.list.dialogs.ConfirmShowRemovalDialog
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

// TODO: mirror to caller when to invalidate the menu
class ShowMenuHelper(
	private val fragment: Fragment,
	private val show: MediathekShow
) : OnMenuItemClickListener {

	private val viewModel: ShowMenuHelperViewModel by fragment.viewModel()

	fun showContextMenu(view: View, listener: OnMenuItemClickListener = this) {
		PopupMenu(fragment.requireContext(), view, Gravity.TOP or Gravity.END).apply {
			inflateShowMenu(menu, menuInflater)
			show()
			setOnMenuItemClickListener(listener)
		}
	}

	fun inflateShowMenu(menu: Menu, menuInflater: MenuInflater) {
		// TODO: use other menu resource
		menuInflater.inflate(R.menu.download_fragment_context, menu)
		// TODO: load persisted show if available
		// TODO: show / hide items
	}

	fun onMenuItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_share -> {
				show.shareExternally(fragment.requireContext())
				true
			}
			R.id.menu_remove -> {
				showConfirmRemovalDialog()
				return true
			}
			// TODO: handle other cases
			else -> false
		}
	}

	override fun onMenuItemClick(item: MenuItem?) =
		if (item == null) false else onMenuItemSelected(item)

	private fun showConfirmRemovalDialog() {
		// TODO: move to other package
		val dialog = ConfirmShowRemovalDialog()

		fragment.setFragmentResultListener(ConfirmShowRemovalDialog.REQUEST_KEY_CONFIRMED) { _, _ ->
			fragment.viewLifecycleOwner.lifecycleScope.launch {
				viewModel.remove(show)
			}
		}

		dialog.show(fragment.parentFragmentManager, null)
	}
}
