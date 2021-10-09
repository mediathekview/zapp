package de.christinecoenen.code.zapp.app.livestream.repository

import de.christinecoenen.code.zapp.app.livestream.api.IZappBackendApiService
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel
import de.christinecoenen.code.zapp.app.livestream.api.model.Show
import de.christinecoenen.code.zapp.app.livestream.api.model.ShowResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class ProgramInfoRepositoryTest {

	private lateinit var zappApi: IZappBackendApiService
	private lateinit var programInfoRepository: ProgramInfoRepository

	@Before
	fun setup() {
		zappApi = mock()
		programInfoRepository = ProgramInfoRepository(zappApi)
	}

	@Test(expected = IllegalArgumentException::class)
	fun testGetShowWithInvalidChannelThrows() = runBlocking {
		programInfoRepository.getShow("invalid-channel-id")
		Unit
	}

	@Test(expected = RuntimeException::class)
	fun testGetShowThrowsWithEmptyApiResult() = runBlocking {
		// prepare
		val showResponse = ShowResponse()
		whenever(zappApi.getShows(any())).thenReturn(showResponse)

		// act
		programInfoRepository.getShow(Channel.ARTE.toString())

		Unit
	}

	@Test
	fun testGetShowComesFromApi() = runBlocking {
		// prepare
		val apiShow = getUpToDateShow()
		val showResponse = ShowResponse(listOf(apiShow))
		whenever(zappApi.getShows(Channel.ARTE.toString()))
			.thenReturn(showResponse)

		// act
		val show = programInfoRepository.getShow(Channel.ARTE.toString())

		// assert
		Assert.assertEquals(apiShow.title, show.title)
		Assert.assertEquals(apiShow.subtitle, show.subtitle)
		Assert.assertEquals(apiShow.description, show.description)
		Assert.assertEquals(apiShow.startTime, show.startTime.toString())
		Assert.assertEquals(apiShow.endTime, show.endTime.toString())
	}

	@Test
	fun testApiIsCalledOnlyOnceForUpToDateShow() = runBlocking {
		// prepare
		val apiShow = getUpToDateShow()
		val showResponse = ShowResponse(listOf(apiShow))
		whenever(zappApi.getShows(Channel.ARTE.toString()))
			.thenReturn(showResponse)

		// act
		programInfoRepository.getShow(Channel.ARTE.toString())
		programInfoRepository.getShow(Channel.ARTE.toString())
		programInfoRepository.getShow(Channel.ARTE.toString())

		// assert
		verify(zappApi, times(1)).getShows(any())

		Unit
	}

	private fun getUpToDateShow(): Show {
		return Show(
			title = "title",
			subtitle = "subtitle",
			description = "description",
			startTime = DateTime.now().minusHours(1).toString(),
			endTime = DateTime.now().plusHours(1).toString(),
		)
	}
}
