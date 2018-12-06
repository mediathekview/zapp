package de.christinecoenen.code.zapp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.model.json.JsonChannelList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Make sure to run this after adding a new channel.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class JsonChannelListTest {

	private IChannelList channelList;

	@Before
	public void setup() {
		channelList = new JsonChannelList(InstrumentationRegistry.getInstrumentation().getTargetContext());
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
			assertFalse(id + " has a color set", channel.getColor() == 0);
			assertFalse(id + " has a drawable id set", channel.getDrawableId() == 0);
			assertTrue(id + " stream is reachable", pingURL(channel.getStreamUrl(), 2000));

			ids.add(id);
		}
	}

	/**
	 * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in
	 * the 200-399 range.
	 *
	 * @param url     The HTTP URL to be pinged.
	 * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that
	 *                the total timeout is effectively two times the given timeout.
	 * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the
	 * given timeout, otherwise <code>false</code>.
	 * @see "http://stackoverflow.com/a/3584332/3012757"
	 */
	private static boolean pingURL(String url, int timeout) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode();
			return (200 <= responseCode && responseCode <= 399);
		} catch (IOException exception) {
			exception.printStackTrace();
			return false;
		}
	}
}
