package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup

import de.christinecoenen.code.zapp.model.ChannelModel
import de.christinecoenen.code.zapp.model.IChannelList


internal class ChannelDetailAdapter(fragmentManager: FragmentManager,
									private val channelList: IChannelList,
									private val listener: OnItemChangedListener) : FragmentStatePagerAdapter(fragmentManager) {

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

	override fun setPrimaryItem(container: ViewGroup, position: Int, selectedElement: Any) {
		if (currentFragment !== selectedElement) {
			// tell old fragment it's no longer visible
			currentFragment?.onHide()

			currentFragment = selectedElement as StreamPageFragment
			listener.onItemSelected(channelList[position])
		}

		super.setPrimaryItem(container, position, selectedElement)
	}

	internal interface OnItemChangedListener {
		fun onItemSelected(model: ChannelModel)
	}
}
