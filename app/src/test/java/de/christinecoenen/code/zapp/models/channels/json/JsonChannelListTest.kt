package de.christinecoenen.code.zapp.models.channels.json

import androidx.test.core.app.ApplicationProvider
import de.christinecoenen.code.zapp.AutoCloseKoinTest
import de.christinecoenen.code.zapp.models.channels.IChannelList
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

/**
 * Make sure to run this after adding a new channel.
 */
@RunWith(RobolectricTestRunner::class)
class JsonChannelListTest : AutoCloseKoinTest() {

	private lateinit var channelList: IChannelList

	@Before
	fun setup() {
		channelList = JsonChannelList(ApplicationProvider.getApplicationContext())
	}

	@Test
	fun basicParsing() {
		Assert.assertNotNull("channel list is not null", channelList)
		Assert.assertTrue("channel list is not empty", channelList.size() > 0)
	}

	@Test
	fun channelsValid() {
		val ids: MutableSet<String> = HashSet()

		for (channel in channelList) {
			Assert.assertNotNull("channel is not null", channel)

			val id = channel.id

			Assert.assertNotNull("$id id is not null", id)
			Assert.assertFalse("$id id is not taken", ids.contains(id))
			Assert.assertNotNull("$id name is not null", channel.name)
			Assert.assertNotNull("$id stream url is not null", channel.streamUrl)
			Assert.assertNotEquals("$id has a color set", 0, channel.color)
			Assert.assertNotEquals("$id has a drawable id set", 0, channel.drawableId)

			ids.add(id)
		}
	}
}
