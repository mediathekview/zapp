package de.christinecoenen.code.zapp.model;

import java.util.List;

public interface IChannelList extends Iterable<ChannelModel> {

	ChannelModel get(int index);
	int size();
	List<ChannelModel> getList();
}
