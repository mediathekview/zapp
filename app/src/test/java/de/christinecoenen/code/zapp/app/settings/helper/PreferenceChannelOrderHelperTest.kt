package de.christinecoenen.code.zapp.app.settings.helper

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.test.core.app.ApplicationProvider
import de.christinecoenen.code.zapp.AutoCloseKoinTest
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
class PreferenceChannelOrderHelperTest : AutoCloseKoinTest() {

	private lateinit var preferenceChannelOrderHelper: PreferenceChannelOrderHelper

	@Before
	fun setup() {
		val context = ApplicationProvider.getApplicationContext<Context>()
		preferenceChannelOrderHelper = PreferenceChannelOrderHelper(context)
	}

	@Test
	fun testSortEmptyChannelList() {
		val sorted = preferenceChannelOrderHelper.sortChannelList(listOf(), false)
		assertSame(0, sorted.size)
	}

	@Test
	fun testSortSmallChannelList() {
		val unsortedList = listOf(
			getDummyChannel(0),
			getDummyChannel(1),
		)

		val sorted = preferenceChannelOrderHelper.sortChannelList(unsortedList, false)
		assertEquals(unsortedList, sorted)
	}

	@Test
	fun testChannelAddedAfterSorting() {
		val savedChannelList = mutableListOf(
			getDummyChannel(0),
			getDummyChannel(1),
		)

		preferenceChannelOrderHelper.saveChannelOrder(savedChannelList)

		val unsortedList = listOf(
			getDummyChannel(2),
			getDummyChannel(1),
			getDummyChannel(0),
		)

		val sorted = preferenceChannelOrderHelper.sortChannelList(unsortedList, false)

		assertEquals(
			listOf(
				getDummyChannel(0),
				getDummyChannel(1),
				getDummyChannel(2),
			),
			sorted
		)
	}

	@Test
	fun testChannelRemovedAfterSorting() {
		val savedChannelList = mutableListOf(
			getDummyChannel(0),
			getDummyChannel(1),
			getDummyChannel(2),
		)

		preferenceChannelOrderHelper.saveChannelOrder(savedChannelList)

		val unsortedList = listOf(
			getDummyChannel(2),
			getDummyChannel(0),
		)

		val sorted = preferenceChannelOrderHelper.sortChannelList(unsortedList, false)

		assertEquals(
			listOf(
				getDummyChannel(0),
				getDummyChannel(2),
			),
			sorted
		)
	}

	@Test
	fun testChannelAddedAndRemovedAfterSorting() {
		val savedChannelList = mutableListOf(
			getDummyChannel(2),
			getDummyChannel(1),
			getDummyChannel(0),
		)

		preferenceChannelOrderHelper.saveChannelOrder(savedChannelList)

		val unsortedList = listOf(
			getDummyChannel(2),
			getDummyChannel(3),
			getDummyChannel(1),
		)

		val sorted = preferenceChannelOrderHelper.sortChannelList(unsortedList, false)

		assertEquals(
			listOf(
				getDummyChannel(2),
				getDummyChannel(1),
				getDummyChannel(3),
			),
			sorted
		)
	}

	private fun getDummyChannel(number: Int): ChannelModel {
		return ChannelModel(
			id = number.toString(),
			name = number.toString(),
			subtitle = null,
			drawableId = R.drawable.channel_logo_br,
			streamUrl = "https://example.com/${number}",
			color = Color.Red.toArgb(),
		)
	}
}
