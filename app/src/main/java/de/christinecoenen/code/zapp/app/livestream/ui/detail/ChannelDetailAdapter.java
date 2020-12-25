package de.christinecoenen.code.zapp.app.livestream.ui.detail;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import de.christinecoenen.code.zapp.models.ChannelModel;
import de.christinecoenen.code.zapp.models.IChannelList;


class ChannelDetailAdapter extends FragmentStatePagerAdapter {

	private final IChannelList channelList;
	private final Listener listener;
	private StreamPageFragment currentFragment;

	ChannelDetailAdapter(FragmentManager fragmentManager, IChannelList channelList,
								Listener listener) {
		super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		this.channelList = channelList;
		this.listener = listener;
	}

	StreamPageFragment getCurrentFragment() {
		return currentFragment;
	}

	ChannelModel getChannel(int index) {
		return channelList.get(index);
	}

	@NonNull
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
	public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		if (currentFragment != object) {
			if (currentFragment != null) {
				// tell old fragment it's no longer visible
				currentFragment.onHide();
			}

			currentFragment = ((StreamPageFragment) object);
			listener.onItemSelected(channelList.get(position));
		}

		super.setPrimaryItem(container, position, object);
	}

	interface Listener {
		void onItemSelected(ChannelModel model);
	}
}
