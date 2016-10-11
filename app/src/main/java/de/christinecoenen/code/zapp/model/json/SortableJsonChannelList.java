package de.christinecoenen.code.zapp.model.json;


import android.content.Context;

import java.util.Iterator;
import java.util.List;

import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.model.ISortableChannelList;
import de.christinecoenen.code.zapp.model.SimpleChannelList;
import de.christinecoenen.code.zapp.preferences.PreferenceChannelOrderHelper;

public class SortableJsonChannelList implements ISortableChannelList {

	private IChannelList channelList;
	private final PreferenceChannelOrderHelper channelOrderHelper;

	public SortableJsonChannelList(Context context) {
		channelOrderHelper = new PreferenceChannelOrderHelper(context);
		channelList = new JsonChannelList(context);
		loadSortingFromDisk();
	}

	@Override
	public ChannelModel get(int index) {
		return channelList.get(index);
	}

	@Override
	public int size() {
		return channelList.size();
	}

	@Override
	public List<ChannelModel> getList() {
		return channelList.getList();
	}

	@Override
	public Iterator<ChannelModel> iterator() {
		return channelList.iterator();
	}

	@Override
	public void reloadChannelOrder() {
		loadSortingFromDisk();
	}

	@Override
	public void persistChannelOrder() {
		channelOrderHelper.saveChannelOrder(channelList.getList());
	}

	private void loadSortingFromDisk() {
		List<ChannelModel> sortedChannels = channelOrderHelper.sortChannelList(channelList.getList());
		channelList = new SimpleChannelList(sortedChannels);
	}
}
