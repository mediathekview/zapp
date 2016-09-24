package de.christinecoenen.code.programguide.plugins;


import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.plugins.ard.ArdDownloader;
import de.christinecoenen.code.programguide.plugins.arte.ArteDownloader;
import de.christinecoenen.code.programguide.plugins.zdf.ZdfDownloader;

public class PluginRegistry {

	private static PluginRegistry instance = null;

	public static PluginRegistry getInstance(Context context) {
		if (instance == null) {
			instance = new PluginRegistry(context);
		}
		return instance;
	}


	private final Map<Channel, IProgramGuideDownloader> downloaders;

	private PluginRegistry(Context context) {
		RequestQueue queue = Volley.newRequestQueue(context);

		downloaders = new HashMap<>(Channel.values().length);

		for (Channel ardChannel : ArdDownloader.CHANNELS) {
			downloaders.put(ardChannel, new ArdDownloader(queue, ardChannel));
		}
		for (Channel zdfChannel : ZdfDownloader.CHANNELS) {
			downloaders.put(zdfChannel, new ZdfDownloader(queue, zdfChannel));
		}
		for (Channel arteChannel : ArteDownloader.CHANNELS) {
			downloaders.put(arteChannel, new ArteDownloader(queue, arteChannel));
		}
	}

	public IProgramGuideDownloader getDownloader(Channel channel) {
		return downloaders.get(channel);
	}
}
