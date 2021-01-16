package de.christinecoenen.code.zapp.models.channels.json

import androidx.test.core.app.ApplicationProvider
import de.christinecoenen.code.zapp.models.channels.IChannelList
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Make sure to run this after adding a new channel.
 */
@RunWith(RobolectricTestRunner::class)
class JsonChannelListConnectionTest: AutoCloseKoinTest() {

	private lateinit var channelList: IChannelList

	@Before
	fun setup() {
		channelList = JsonChannelList(ApplicationProvider.getApplicationContext())
	}

	@Test
	fun channelsValid() {
		for (channel in channelList) {
			Assert.assertTrue("${channel.id} stream is reachable", pingURL(channel.streamUrl, 2000))
		}
	}

	companion object {
		/**
		 * Pings a HTTP URL. This effectively sends a HEAD request and returns `true` if the response code is in
		 * the 200-399 range.
		 *
		 * @param url     The HTTP URL to be pinged.
		 * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that
		 * the total timeout is effectively two times the given timeout.
		 * @return `true` if the given HTTP URL has returned response code 200-399 on a HEAD request within the
		 * given timeout, otherwise `false`.
		 * @see "http://stackoverflow.com/a/3584332/3012757"
		 */
		@Suppress("SameParameterValue")
		private fun pingURL(url: String, timeout: Int): Boolean {
			return try {
				val connection = URL(url).openConnection() as HttpURLConnection
				connection.connectTimeout = timeout
				connection.readTimeout = timeout
				connection.requestMethod = "GET"
				connection.responseCode in 200..399
			} catch (exception: IOException) {
				exception.printStackTrace()
				false
			}
		}
	}
}
