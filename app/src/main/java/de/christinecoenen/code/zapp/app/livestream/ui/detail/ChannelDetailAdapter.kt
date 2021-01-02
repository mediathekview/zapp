package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.channels.IChannelList

internal class ChannelDetailAdapter(
	fragmentManager: FragmentManager,
	private val channelList: IChannelList,
	private val listener: Listener
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

	var currentFragment: StreamPageFragment? = null
		private set

	fun getChannel(index: Int): ChannelModel {
		return channelList[index]
	}

	override fun getItem(position: Int): Fragment {
		val channelModel = channelList[position]
		return StreamPageFragment.newInstance(channelModel)
	}

	override fun getCount(): Int {
		return channelList.size()
	}

	override fun getPageTitle(position: Int): CharSequence? {
		return channelList[position].name
	}

	override fun setPrimaryItem(container: ViewGroup, position: Int, fragmentArgument: Any) {
		super.setPrimaryItem(container, position, fragmentArgument)

		if (currentFragment == fragmentArgument) {
			return
		}

		// tell old fragment it's no longer visible
		currentFragment?.onHide()

		currentFragment = fragmentArgument as StreamPageFragment

		listener.onItemSelected(channelList[position])
	}

	interface Listener {
		fun onItemSelected(channel: ChannelModel)
	}
}
