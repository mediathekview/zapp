package de.christinecoenen.code.zapp.app.settings.ui

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.ChannelSelectionFragmentBinding
import de.christinecoenen.code.zapp.models.channels.ISortableChannelList
import de.christinecoenen.code.zapp.models.channels.json.SortableJsonChannelList
import de.christinecoenen.code.zapp.utils.view.GridAutofitLayoutManager
import de.christinecoenen.code.zapp.utils.view.SimpleDragListListener

class ChannelSelectionFragment : Fragment(), MenuProvider {

	private var _binding: ChannelSelectionFragmentBinding? = null
	private val binding: ChannelSelectionFragmentBinding get() = _binding!!


	private lateinit var channelList: ISortableChannelList
	private lateinit var listAdapter: ChannelSelectionAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		channelList = SortableJsonChannelList(requireContext())
		listAdapter = ChannelSelectionAdapter(requireContext())
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = ChannelSelectionFragmentBinding.inflate(inflater, container, false)

		// list adapter
		listAdapter.itemList = channelList.list

		// view
		val layoutManager = GridAutofitLayoutManager(requireContext(), 120)

		binding.draglistChannelSelection.apply {

			setLayoutManager(layoutManager)
			setAdapter(listAdapter, true)
			recyclerView.isVerticalScrollBarEnabled = true

			setDragListListener(object : SimpleDragListListener() {
				override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {
					if (fromPosition != toPosition) {
						channelList.persistChannelOrder()
					}
				}
			})
		}

		requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

		return binding.root
	}

	override fun onPause() {
		super.onPause()

		channelList.persistChannelOrder()
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.channel_selection_fragment, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_help -> {
				openHelpDialog()
				true
			}
			else -> false
		}
	}

	private fun openHelpDialog() {
		val directions =
			ChannelSelectionFragmentDirections.toChannelSelectionHelpDialog()
		findNavController().navigate(directions)
	}
}
