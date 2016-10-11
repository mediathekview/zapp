package de.christinecoenen.code.zapp.preferences;


import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.utils.PreferenceHelper;


/**
 * Helps persisting and reloading the channel order.
 * SharedPreferences are used to store the values on disk.
 */
public class PreferenceChannelOrderHelper {

	private static final String PREF_KEY_CHANNEL_ORDER = "PREF_KEY_CHANNEL_ORDER";

	private final PreferenceHelper preferenceHelper;

	public PreferenceChannelOrderHelper (Context context) {
		preferenceHelper = new PreferenceHelper(context);
	}

	public void saveChannelOrder(List<ChannelModel> channels) {
		List<String> sortedChannelIds = new ArrayList<>(channels.size());

		for (ChannelModel channel : channels) {
			sortedChannelIds.add(channel.getId());
		}

		preferenceHelper.saveList(PREF_KEY_CHANNEL_ORDER, sortedChannelIds);
	}

	public List<ChannelModel> sortChannelList(List<ChannelModel> channels) {
		List<String> sortedChannelIds = preferenceHelper.loadList(PREF_KEY_CHANNEL_ORDER);

		if (sortedChannelIds == null) {
			// have never been saved before
			return  channels;
		}

		int size = Math.max(sortedChannelIds.size(), channels.size());
		int unsavedIndex = sortedChannelIds.size();

		ChannelModel[] sortedChannelArray = new ChannelModel[size];

		for (ChannelModel channel : channels) {
			int index = sortedChannelIds.indexOf(channel.getId());

			if (index == -1) {
				// order for this channel has never been saved - move to end
				index = unsavedIndex++;
			}

			sortedChannelArray[index] = channel;
		}

		// save as editable list without null values in case channels have been deleted
		List<ChannelModel> sortedChannelList = new ArrayList<>(Arrays.asList(sortedChannelArray));
		sortedChannelList.removeAll(Collections.singleton((ChannelModel) null));

		return sortedChannelList;
	}
}
