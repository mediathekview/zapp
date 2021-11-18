package de.christinecoenen.code.zapp.tv.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.ListItemListener
import de.christinecoenen.code.zapp.databinding.TvFragmentAboutBinding
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.tv.player.PlayerActivity


class AboutFragment : Fragment(), ListItemListener {

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val binding = TvFragmentAboutBinding.inflate(inflater, container, false)

		binding.grid.adapter = AboutListAdapter()
		binding.grid.layoutManager = GridLayoutManager(requireContext(), 2)

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
