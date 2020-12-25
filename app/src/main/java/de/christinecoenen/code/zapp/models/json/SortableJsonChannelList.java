package de.christinecoenen.code.zapp.models.json;


import android.content.Context;
import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.List;

import de.christinecoenen.code.zapp.models.ChannelModel;
import de.christinecoenen.code.zapp.models.IChannelList;
import de.christinecoenen.code.zapp.models.ISortableChannelList;
import de.christinecoenen.code.zapp.models.SimpleChannelList;
import de.christinecoenen.code.zapp.app.settings.helper.PreferenceChannelOrderHelper;

public class SortableJsonChannelList implements ISortableChannelList {

	final Context context;
	final PreferenceChannelOrderHelper channelOrderHelper;
	IChannelList channelList;

	public SortableJsonChannelList(Context context) {
		this.context = context;
		channelOrderHelper = new PreferenceChannelOrderHelper(context);
		reload();
	}

	@Override
	public void reload() {
		loadSortingFromDisk();
	}

	@Override
	public ChannelModel get(int index) {
		return channelList.get(index);
	}

	@Override
	public ChannelModel get(String id) {
		return channelList.get(id);
	}

	@Override
	public int size() {
		return channelList.size();
	}

	@Override
	public List<ChannelModel> getList() {
		return channelList.getList();
	}

	@NonNull
	@Override
	public Iterator<ChannelModel> iterator() {
		return channelList.iterator();
	}

	@Override
	public int indexOf(String channelId) {
		return channelList.indexOf(channelId);
	}

	@Override
	public void reloadChannelOrder() {
		loadSortingFromDisk();
	}

	@Override
	public void persistChannelOrder() {
		channelOrderHelper.saveChannelOrder(channelList.getList());
	}

	void loadSortingFromDisk() {
		List<ChannelModel> listFromDisk = new JsonChannelList(context).getList();
		List<ChannelModel> sortedChannels = channelOrderHelper.sortChannelList(listFromDisk, false);
		channelList = new SimpleChannelList(sortedChannels);
	}
}
