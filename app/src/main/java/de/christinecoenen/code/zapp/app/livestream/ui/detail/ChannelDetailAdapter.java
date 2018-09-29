package de.christinecoenen.code.zapp.app.livestream.ui.detail;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;


class ChannelDetailAdapter extends FragmentStatePagerAdapter {

	private final IChannelList channelList;
	private final OnItemChangedListener listener;
	private StreamPageFragment currentFragment;

	ChannelDetailAdapter(FragmentManager fragmentManager, IChannelList channelList,
								OnItemChangedListener listener) {
		super(fragmentManager);
		this.channelList = channelList;
		this.listener = listener;
	}

	StreamPageFragment getCurrentFragment() {
		return currentFragment;
	}

	ChannelModel getChannel(int index) {
		return channelList.get(index);
	}

	@Override
	public Fragment getItem(int position) {
		ChannelModel channelModel = channelList.get(position);
		return StreamPageFragment.newInstance(channelModel);
	}

	@Override
	public int getCount() {
		return channelList.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return channelList.get(position).getName();
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if (currentFragment != object) {
			if (currentFragment != null) {
				// tell old fragment it's no longer visible
				currentFragment.onHide();
			}

			currentFragment = ((StreamPageFragment) object);
			listener.OnItemSelected(channelList.get(position));
		}

		super.setPrimaryItem(container, position, object);
	}

	interface OnItemChangedListener {
		void OnItemSelected(ChannelModel model);
	}
}
