package de.christinecoenen.code.zapp.app.livestream.ui.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.app.livestream.ui.detail.ChannelPlayerActivity
import de.christinecoenen.code.zapp.app.livestream.ui.detail.ProgramInfoSheetDialogFragment
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.BaseChannelListAdapter
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.ChannelListAdapter
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.ListItemListener
import de.christinecoenen.code.zapp.databinding.ChannelListFragmentBinding
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.channels.ISortableChannelList
import de.christinecoenen.code.zapp.models.channels.json.SortableVisibleJsonChannelList
import de.christinecoenen.code.zapp.utils.view.GridAutofitLayoutManager

class ChannelListFragment : Fragment(), MenuProvider, ListItemListener {

	private lateinit var channelList: ISortableChannelList
	private lateinit var gridAdapter: BaseChannelListAdapter


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		channelList = SortableVisibleJsonChannelList(requireContext())
		gridAdapter = ChannelListAdapter(channelList, this, this)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val binding = ChannelListFragmentBinding.inflate(inflater, container, false)
		val channelGridView = binding.gridviewChannels

		ViewCompat.setNestedScrollingEnabled(channelGridView, true)
		channelGridView.layoutManager = GridAutofitLayoutManager(requireContext(), 400)
		channelGridView.adapter = gridAdapter

		requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

		return binding.root
	}

	@SuppressLint("NotifyDataSetChanged")
	override fun onResume() {
		super.onResume()

		channelList.reloadChannelOrder()

		// We do not know if the channels changed, so we need to reload them all.
		// Otherwise outdated data may cause null pointers in the adapter.
		gridAdapter.notifyDataSetChanged()
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.activity_main_toolbar, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false

	override fun onItemClick(channel: ChannelModel) {
		val intent = ChannelPlayerActivity.getStartIntent(context, channel.id)
		startActivity(intent)
	}

	override fun onItemLongClick(
		channel: ChannelModel,
		programInfoViewModel: ProgramInfoViewModel,
		view: View
	) {
		val menu = PopupMenu(context, view, Gravity.TOP or Gravity.END)
		menu.inflate(R.menu.channel_list_fragment_context)
		menu.show()
		menu.setOnMenuItemClickListener { menuItem ->
			onContextMenuItemClicked(menuItem, channel, programInfoViewModel)
		}
	}

	private fun onContextMenuItemClicked(
		menuItem: MenuItem,
		channel: ChannelModel,
		programInfoViewModel: ProgramInfoViewModel
	): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_share -> {
				channel.playExternally(requireContext())
				true
			}

			R.id.menu_program_info -> {
				val modalBottomSheet = ProgramInfoSheetDialogFragment(
					programInfoViewModel,
					ProgramInfoSheetDialogFragment.Size.Large
				)
				modalBottomSheet.show(parentFragmentManager, ProgramInfoSheetDialogFragment.TAG)
				true
			}

			else -> false
		}
	}
}
