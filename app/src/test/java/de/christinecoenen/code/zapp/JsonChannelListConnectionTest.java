package de.christinecoenen.code.zapp;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.model.json.JsonChannelList;

import static org.junit.Assert.assertTrue;

/**
 * Make sure to run this after adding a new channel.
 */
@RunWith(RobolectricTestRunner.class)
public class JsonChannelListConnectionTest {

	private IChannelList channelList;

	@Before
	public void setup() {
		channelList = new JsonChannelList(ApplicationProvider.getApplicationContext());
	}

	@Test
	public void channelsValid() {
		for (int i = 0; i < channelList.size(); i++) {
			ChannelModel channel = channelList.get(i);

			String id = channel.getId();
			assertTrue(id + " stream is reachable", pingURL(channel.getStreamUrl(), 2000));
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
