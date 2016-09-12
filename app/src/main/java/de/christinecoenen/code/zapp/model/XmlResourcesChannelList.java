package de.christinecoenen.code.zapp.model;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Debug;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.christinecoenen.code.zapp.R;

public class XmlResourcesChannelList implements IChannelList {

	private String[] channelNames;
	private String[] channelUrls;
	private TypedArray channelLogoIds;

	List<ChannelModel> channels = new ArrayList<>();

	public XmlResourcesChannelList(Context context) {
		channelNames = context.getResources().getStringArray(R.array.channel_names);
		channelUrls = context.getResources().getStringArray(R.array.channel_urls);
		channelLogoIds = context.getResources().obtainTypedArray(R.array.channel_logos);

		for (int i = 0; i < channelNames.length; i++) {
			channels.add(createChannel(i));
		}

		channelLogoIds.recycle();
	}

	@Override
	public ChannelModel get(int index) {
		return channels.get(index);
	}

	@Override
	public Iterator<ChannelModel> iterator() {
		return channels.iterator();
	}

	@Override
	public int size() {
		return channels.size();
	}

	protected ChannelModel createChannel(int index) {
		ChannelModel channel = new ChannelModel();
		channel.setName(channelNames[index]);
		channel.setStreamUrl(channelUrls[index]);
		channel.setDrawableId(channelLogoIds.getResourceId(index, -1));
		return channel;
	}
}
