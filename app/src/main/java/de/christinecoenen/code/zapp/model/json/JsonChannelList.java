package de.christinecoenen.code.zapp.model.json;


import android.content.Context;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;

/**
 * Loads channel data from a json file bundled with
 * the app. This operation is blocking. For the number
 * of channels provided by the app this approach works
 * just fine.
 */
public class JsonChannelList implements IChannelList {

	private final Context context;
	private final List<ChannelModel> channels;

	public JsonChannelList(Context context) {
		this.context = context;
		JsonChannelsParser parser = new JsonChannelsParser(context);
		channels = parser.parse(getJsonString());
	}

	@Override
	public ChannelModel get(int index) {
		return channels.get(index);
	}

	@Override
	public int size() {
		return channels.size();
	}

	/**
	 * @return content of R.raw.channels json file
     */
	private String getJsonString() {
		InputStream inputStream = context.getResources().openRawResource(R.raw.channels);
		String channelJson = null;

		try {
			channelJson = IOUtils.toString(inputStream, "UTF-8");
		} catch (IOException e) {
			// we know, this file is bundled with the app,
			// so this should never happen
			e.printStackTrace();
		}

		IOUtils.closeQuietly(inputStream);
		return channelJson;
	}
}
