package de.christinecoenen.code.zapp.app.downloads.ui.list

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.downloads.ui.list.adapter.DownloadListAdapter
import de.christinecoenen.code.zapp.app.downloads.ui.list.dialogs.ConfirmShowRemovalDialog
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragmentDirections
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentBinding
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class DownloadsFragment : Fragment(), DownloadListAdapter.Listener, MenuProvider {

	private var _binding: DownloadsFragmentBinding? = null
	private val binding: DownloadsFragmentBinding get() = _binding!!

	private val viewModel: DownloadsViewModel by viewModel()
	private lateinit var downloadAdapter: DownloadListAdapter

	private var longClickShow: PersistedMediathekShow? = null

	private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
		override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
			updateNoDownloadsVisibility()
		}

		override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
			updateNoDownloadsVisibility()
		}

		override fun onStateRestorationPolicyChanged() {
			updateNoDownloadsVisibility()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		downloadAdapter = DownloadListAdapter(lifecycleScope, this, viewModel)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DownloadsFragmentBinding.inflate(inflater, container, false)

		binding.list.adapter = downloadAdapter

		downloadAdapter.registerAdapterDataObserver(adapterDataObserver)

		binding.search.addTextChangedListener { editable ->
			viewModel.setSearchQueryFilter(editable.toString())
		}

		viewModel.downloadList.observe(viewLifecycleOwner) {
			lifecycleScope.launchWhenCreated {
				downloadAdapter.submitData(it)
			}
		}
		updateNoDownloadsVisibility()

		requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		downloadAdapter.unregisterAdapterDataObserver(adapterDataObserver)
		_binding = null
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.activity_main_toolbar, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false

	override fun onShowClicked(show: PersistedMediathekShow) {
		val directions =
			MediathekListFragmentDirections.toMediathekDetailFragment(show.mediathekShow)
		findNavController().navigate(directions)
	}

	override fun onShowLongClicked(show: PersistedMediathekShow, view: View) {
		longClickShow = show

		PopupMenu(context, view, Gravity.TOP or Gravity.END).apply {
			inflate(R.menu.download_fragment_context)
			show()
			setOnMenuItemClickListener(::onContextMenuItemClicked)
		}
	}

	private fun onContextMenuItemClicked(menuItem: MenuItem): Boolean {
		when (menuItem.itemId) {
			R.id.menu_share -> {
				longClickShow?.mediathekShow?.shareExternally(requireContext())
				return true
			}
			R.id.menu_remove -> {
				showConfirmRmovalDialog(longClickShow!!)
				return true
			}
		}
		return false
	}

	private fun showConfirmRmovalDialog(show: PersistedMediathekShow) {
		val dialog = ConfirmShowRemovalDialog()

		setFragmentResultListener(ConfirmShowRemovalDialog.REQUEST_KEY_CONFIRMED) { _, _ ->
			viewLifecycleOwner.lifecycleScope.launch {
				viewModel.remove(show)
			}
		}

		dialog.show(parentFragmentManager, null)
	}

	private fun updateNoDownloadsVisibility() {
		binding.noDownloads.isVisible = downloadAdapter.itemCount == 0
	}
}
