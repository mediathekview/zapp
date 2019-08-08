package de.christinecoenen.code.zapp.model.json;


import android.content.Context;

import java.util.List;

import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.SimpleChannelList;

public class SortableVisibleJsonChannelList extends SortableJsonChannelList {

	public SortableVisibleJsonChannelList(Context context) {
		super(context);
	}

	protected void loadSortingFromDisk() {
		List<ChannelModel> listFromDisk = new JsonChannelList(context).getList();
		List<ChannelModel> sortedChannels = channelOrderHelper.sortChannelList(listFromDisk, true);
		channelList = new SimpleChannelList(sortedChannels);
	}
}
