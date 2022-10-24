package de.christinecoenen.code.zapp.tv.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.ListItemListener
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.databinding.TvFragmentChannelListBinding
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.repositories.ChannelRepository
import de.christinecoenen.code.zapp.tv.player.PlayerActivity
import org.koin.android.ext.android.inject


class ChannelListFragment : Fragment(), ListItemListener {

	private val channelRepository: ChannelRepository by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val binding = TvFragmentChannelListBinding.inflate(inflater, container, false)

		binding.grid.setNumColumns(2)
		binding.grid.adapter = ChannelListAdapter(channelRepository.getChannelList(), this, this)

		return binding.root
	}

	override fun onItemClick(channel: ChannelModel) {
		val intent = PlayerActivity.getStartIntent(requireContext(), VideoInfo.fromChannel(channel))
		startActivity(intent)
	}

	override fun onItemLongClick(channel: ChannelModel, view: View) {
		// no action
	}
}
