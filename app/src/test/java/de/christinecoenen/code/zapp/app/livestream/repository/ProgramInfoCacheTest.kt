package de.christinecoenen.code.zapp.app.livestream.repository

import de.christinecoenen.code.zapp.app.livestream.api.model.Channel
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ProgramInfoCacheTest {

	private lateinit var programInfoCache: ProgramInfoCache

	@Before
	fun setup() {
		programInfoCache = ProgramInfoCache()
	}

	@Test
	fun testEmtyCacheReturnsNull() {
		Assert.assertNull(programInfoCache.getShow(Channel.ARD_ALPHA))
		Assert.assertNull(programInfoCache.getShow(Channel.ARTE))
		Assert.assertNull(programInfoCache.getShow(Channel.DEUTSCHE_WELLE))
	}

	@Test
	fun testUpToDateShowIsCached() {
		// prepare
		val upToDateShow = LiveShow(
			title = "title",
			startTime = DateTime.now().minusHours(1),
			endTime = DateTime.now().plusHours(1)
		)

		// act
		programInfoCache.save(Channel.ARTE, upToDateShow)
		val cachedShow1 = programInfoCache.getShow(Channel.ARTE)
		val cachedShow2 = programInfoCache.getShow(Channel.ARTE)

		// assert
		Assert.assertEquals(upToDateShow, cachedShow1)
		Assert.assertEquals(upToDateShow, cachedShow2)
	}

	@Test
	fun testOldShowIsNotCached() {
		// prepare
		val oldShow = LiveShow(
			title = "title",
			startTime = DateTime.now().minusHours(2),
			endTime = DateTime.now().minusHours(1)
		)

		// act
		programInfoCache.save(Channel.ARTE, oldShow)
		val cachedShow = programInfoCache.getShow(Channel.ARTE)

		// assert
		Assert.assertNull(cachedShow)
	}
}
