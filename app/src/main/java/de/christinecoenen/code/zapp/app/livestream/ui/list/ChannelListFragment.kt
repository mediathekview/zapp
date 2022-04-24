package de.christinecoenen.code.zapp.app.livestream.ui.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.livestream.ui.detail.ChannelPlayerActivity
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.BaseChannelListAdapter
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.ChannelListAdapter
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.ListItemListener
import de.christinecoenen.code.zapp.databinding.ChannelListFragmentBinding
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.channels.ISortableChannelList
import de.christinecoenen.code.zapp.models.channels.json.SortableVisibleJsonChannelList
import de.christinecoenen.code.zapp.utils.view.GridAutofitLayoutManager

class ChannelListFragment : Fragment(), ListItemListener {

	private lateinit var channelList: ISortableChannelList
	private lateinit var gridAdapter: BaseChannelListAdapter


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		channelList = SortableVisibleJsonChannelList(requireContext())
		gridAdapter = ChannelListAdapter(channelList, this, this)

		setHasOptionsMenu(true)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val binding = ChannelListFragmentBinding.inflate(inflater, container, false)
		val channelGridView = binding.gridviewChannels

		ViewCompat.setNestedScrollingEnabled(channelGridView, true)
		channelGridView.layoutManager = GridAutofitLayoutManager(requireContext(), 320)
		channelGridView.adapter = gridAdapter

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

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.activity_main_toolbar, menu)
	}

	override fun onItemClick(channel: ChannelModel) {
		val intent = ChannelPlayerActivity.getStartIntent(context, channel.id)
		startActivity(intent)
	}

	override fun onItemLongClick(channel: ChannelModel, view: View) {
		val menu = PopupMenu(context, view, Gravity.TOP or Gravity.END)
		menu.inflate(R.menu.channel_list_fragment_context)
		menu.show()
		menu.setOnMenuItemClickListener { menuItem -> onContextMenuItemClicked(menuItem, channel) }
	}

	private fun onContextMenuItemClicked(menuItem: MenuItem, channel: ChannelModel): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_share -> {
				channel.playExternally(requireContext())
				true
			}
			else -> false
		}
	}
}
