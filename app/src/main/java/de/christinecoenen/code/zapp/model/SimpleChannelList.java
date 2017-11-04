package de.christinecoenen.code.zapp.model;


import android.support.annotation.NonNull;

import java.util.Iterator;
import java.util.List;

/**
 * Simple IChannelList wrapper around a List of ChannelModels.
 */
public class SimpleChannelList implements IChannelList {

	private final List<ChannelModel> channels;

	public SimpleChannelList(List<ChannelModel> channels) {
		this.channels = channels;
	}

	@Override
	public ChannelModel get(int index) {
		return channels.get(index);
	}

	@Override
	public ChannelModel get(String id) {
		int index = indexOf(id);
		return index == -1 ? null : get(index);
	}

	@Override
	public int size() {
		return channels.size();
	}

	@Override
	public List<ChannelModel> getList() {
		return channels;
	}

	@Override
	public int indexOf(String channelId) {
		for (int i = 0; i < channels.size(); i++) {
			if (channels.get(i).getId().equals(channelId)) {
				return i;
			}
		}
		return -1;
	}

	@NonNull
	@Override
	public Iterator<ChannelModel> iterator() {
		return channels.iterator();
	}
}
