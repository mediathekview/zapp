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
class CacheTest {

	private lateinit var cache: Cache

	@Before
	fun setup() {
		cache = Cache()
	}

	@Test
	fun testEmtyCacheReturnsNull() {
		Assert.assertNull(cache.getShow(Channel.ARD_ALPHA))
		Assert.assertNull(cache.getShow(Channel.ARTE))
		Assert.assertNull(cache.getShow(Channel.DEUTSCHE_WELLE))
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
		cache.save(Channel.ARTE, upToDateShow)
		val cachedShow1 = cache.getShow(Channel.ARTE)
		val cachedShow2 = cache.getShow(Channel.ARTE)

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
		cache.save(Channel.ARTE, oldShow)
		val cachedShow = cache.getShow(Channel.ARTE)

		// assert
		Assert.assertNull(cachedShow)
	}
}
