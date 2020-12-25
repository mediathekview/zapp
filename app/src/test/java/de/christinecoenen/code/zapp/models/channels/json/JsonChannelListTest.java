package de.christinecoenen.code.zapp.models.channels.json;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashSet;
import java.util.Set;

import de.christinecoenen.code.zapp.models.channels.ChannelModel;
import de.christinecoenen.code.zapp.models.channels.IChannelList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Make sure to run this after adding a new channel.
 */
@RunWith(RobolectricTestRunner.class)
public class JsonChannelListTest {

	private IChannelList channelList;

	@Before
	public void setup() {
		channelList = new JsonChannelList(ApplicationProvider.getApplicationContext());
	}

	@Test
	public void basicParsing() {
		assertNotNull("channel list is not null", channelList);
		assertTrue("channel list is not empty", channelList.size() > 0);
	}

	@Test
	public void channelsValid() {
		Set<String> ids = new HashSet<>();

		for (int i = 0; i < channelList.size(); i++) {
			ChannelModel channel = channelList.get(i);
			assertNotNull("channel is not null", channel);

			String id = channel.getId();

			assertNotNull(id + " id is not null", channel.getId());
			assertFalse(id + " id is not taken", ids.contains(id));
			assertNotNull(id + " name is not null", channel.getName());
			assertNotNull(id + " stream url is not null", channel.getStreamUrl());
			assertNotEquals(id + " has a color set", 0, channel.getColor());
			assertNotEquals(id + " has a drawable id set", 0, channel.getDrawableId());

			ids.add(id);
		}
	}
}
