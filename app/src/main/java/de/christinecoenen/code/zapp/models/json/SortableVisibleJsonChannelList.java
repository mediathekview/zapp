package de.christinecoenen.code.zapp.models.json;


import android.content.Context;

import java.util.List;

import de.christinecoenen.code.zapp.models.ChannelModel;
import de.christinecoenen.code.zapp.models.SimpleChannelList;

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
