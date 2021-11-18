package de.christinecoenen.code.zapp.tv.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.ChannelListAdapter
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.ListItemListener
import de.christinecoenen.code.zapp.databinding.TvFragmentChannelListBinding
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.channels.ISortableChannelList
import de.christinecoenen.code.zapp.models.channels.json.SortableVisibleJsonChannelList
import de.christinecoenen.code.zapp.tv.player.PlayerActivity


class ChannelListFragment : Fragment(), ListItemListener {

	private lateinit var channelList: ISortableChannelList

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val binding = TvFragmentChannelListBinding.inflate(inflater, container, false)

		channelList = SortableVisibleJsonChannelList(requireContext())

		binding.grid.setNumColumns(2)
		binding.grid.adapter = ChannelListAdapter(channelList, this, this)

		return binding.root
	}

	override fun onItemClick(channel: ChannelModel) {
		val intent = PlayerActivity.getStartIntent(requireContext(), channel)
		startActivity(intent)
	}

	override fun onItemLongClick(channel: ChannelModel, view: View) {
		// no action
	}
}
