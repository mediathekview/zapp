package de.christinecoenen.code.zapp.app.livestream.ui.detail;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;


public class ChannelDetailAdapter extends FragmentStatePagerAdapter {

	private final IChannelList channelList;
	private final OnItemChangedListener listener;
	private StreamPageFragment currentFragment;

	public ChannelDetailAdapter(FragmentManager fragmentManager, IChannelList channelList,
								OnItemChangedListener listener) {
		super(fragmentManager);
		this.channelList = channelList;
		this.listener = listener;
	}

	public StreamPageFragment getCurrentFragment() {
		return currentFragment;
	}

	public ChannelModel getChannel(int index) {
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

	public interface OnItemChangedListener {
		void OnItemSelected(ChannelModel model);
	}
}
