package de.christinecoenen.code.zapp.model;

import java.util.Iterator;

public interface IChannelList {

	ChannelModel get(int index);
	Iterator<ChannelModel> iterator();
	int size();
}
