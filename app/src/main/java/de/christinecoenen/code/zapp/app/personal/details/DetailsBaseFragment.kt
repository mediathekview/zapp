package de.christinecoenen.code.zapp.app.personal.details

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.downloads.ui.list.dialogs.ConfirmShowRemovalDialog
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragmentDirections
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowListItemListener
import de.christinecoenen.code.zapp.app.personal.details.adapter.PagedPersistedShowListAdapter
import de.christinecoenen.code.zapp.databinding.PersonalDetailsFragmentBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.launch

abstract class DetailsBaseFragment : Fragment(), MediathekShowListItemListener {

	private var _binding: PersonalDetailsFragmentBinding? = null
	private val binding: PersonalDetailsFragmentBinding get() = _binding!!

	protected abstract val viewModel: DetailsBaseViewModel

	private lateinit var showAdapter: PagedPersistedShowListAdapter

	private var longClickShow: MediathekShow? = null

	private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
		override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
			updateNoShowsVisibility()
		}

		override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
			updateNoShowsVisibility()
		}

		override fun onStateRestorationPolicyChanged() {
			updateNoShowsVisibility()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		showAdapter = PagedPersistedShowListAdapter(lifecycleScope, this)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = PersonalDetailsFragmentBinding.inflate(inflater, container, false)

		binding.list.adapter = showAdapter

		showAdapter.registerAdapterDataObserver(adapterDataObserver)

		binding.search.addTextChangedListener { editable ->
			viewModel.setSearchQueryFilter(editable.toString())
		}

		viewModel.showList.observe(viewLifecycleOwner) {
			lifecycleScope.launchWhenCreated {
				showAdapter.submitData(it)
			}
		}

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		showAdapter.unregisterAdapterDataObserver(adapterDataObserver)
		_binding = null
	}

	override fun onShowClicked(show: MediathekShow) {
		val directions =
			MediathekListFragmentDirections.toMediathekDetailFragment(show)
		findNavController().navigate(directions)
	}

	override fun onShowLongClicked(show: MediathekShow, view: View) {
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
				longClickShow?.shareExternally(requireContext())
				return true
			}
			R.id.menu_remove -> {
				showConfirmRemovalDialog(longClickShow!!)
				return true
			}
		}
		return false
	}

	private fun showConfirmRemovalDialog(show: MediathekShow) {
		val dialog = ConfirmShowRemovalDialog()

		setFragmentResultListener(ConfirmShowRemovalDialog.REQUEST_KEY_CONFIRMED) { _, _ ->
			viewLifecycleOwner.lifecycleScope.launch {
				viewModel.remove(show)
			}
		}

		dialog.show(parentFragmentManager, null)
	}

	private fun updateNoShowsVisibility() {
		binding.noShows.isVisible = showAdapter.itemCount == 0
	}
}
