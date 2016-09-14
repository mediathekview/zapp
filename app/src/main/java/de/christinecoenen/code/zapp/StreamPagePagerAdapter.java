package de.christinecoenen.code.zapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;


class StreamPagePagerAdapter extends FragmentStatePagerAdapter {

	private IChannelList channelList;
	private StreamPageFragment currentFragment;
	private OnItemChangedListener listener;

	StreamPagePagerAdapter(FragmentManager fragmentManager, IChannelList channelList,
								  OnItemChangedListener listener) {
		super(fragmentManager);
		this.channelList = channelList;
		this.listener = listener;
	}

	StreamPageFragment getCurrentFragment() {
		return currentFragment;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment = new StreamPageFragment();
		Bundle args = new Bundle();
		args.putSerializable(StreamPageFragment.ARG_CHANNEL_MODEL, channelList.get(position));
		fragment.setArguments(args);
		return fragment;
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
