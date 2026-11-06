package de.christinecoenen.code.zapp.models.shows

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EpisodeNumberTest {

	@Test
	fun testSeasonAndEpisode() {
		assertEquals(EpisodeNumber(20, 7), show("Haukes Dinner (S20/E07)").episodeNumber())
		assertEquals(EpisodeNumber(22, 15), show("Miami, Ibiza, Wismar (S22/E15)").episodeNumber())
		assertEquals(EpisodeNumber(3, 12), show("Der Fall (Staffel 3, Folge 12)").episodeNumber())
		assertEquals(EpisodeNumber(2, 5), show("Der Fall (2x05)").episodeNumber())
	}

	@Test
	fun testSeasonSlashEpisode() {
		assertEquals(EpisodeNumber(3, 12), show("Der Fall (3/12)").episodeNumber())
	}

	@Test
	fun testEpisodeOnly() {
		assertEquals(EpisodeNumber(null, 123), show("Der Fall (123)").episodeNumber())
		assertEquals(EpisodeNumber(null, 7), show("Der Fall (Folge 7)").episodeNumber())
		assertEquals(EpisodeNumber(null, 3), show("Der Fall (Teil 3)").episodeNumber())
		assertEquals(EpisodeNumber(null, 5), show("Der Fall [Folge 5]").episodeNumber())
	}

	@Test
	fun testSeasonOnly() {
		assertEquals(EpisodeNumber(7, null), show("Der Fall (Staffel 7)").episodeNumber())
	}

	@Test
	fun testMultipleParenthesis() {
		assertEquals(
			EpisodeNumber(20, 7),
			show("Haukes Dinner (S20/E07) (Audiodeskription)").episodeNumber()
		)
	}

	@Test
	fun testNoEpisode() {
		assertNull(show("Ostsee, Hanse, SOKO: Wismar").episodeNumber())
		assertNull(show("Der Fall (Audiodeskription)").episodeNumber())
	}

	@Test
	fun testValuesOfMediathekArePreferred() {
		val show = show("Der Fall (123)").copy(season = 4, episode = 9)
		assertEquals(EpisodeNumber(4, 9), show.episodeNumber())
	}

	@Test
	fun testSortByEpisode() {
		val shows = listOf(
			show("C (S2/E1)"),
			show("ohne Nummer"),
			show("A (S1/E2)"),
			show("B (S1/E1)"),
		)

		assertEquals(
			listOf("B (S1/E1)", "A (S1/E2)", "C (S2/E1)", "ohne Nummer"),
			shows.sortedWith(EpisodeComparator()).map { it.title }
		)
	}

	@Test
	fun testSortByEpisodeDescending() {
		val shows = listOf(
			show("C (S2/E1)"),
			show("ohne Nummer"),
			show("A (S1/E2)"),
			show("B (S1/E1)"),
		)

		assertEquals(
			listOf("C (S2/E1)", "A (S1/E2)", "B (S1/E1)", "ohne Nummer"),
			shows.sortedWith(EpisodeComparator(descending = true)).map { it.title }
		)
	}

	private fun show(title: String) = MediathekShow(
		apiId = "id-$title",
		title = title,
		channel = "ZDF",
		videoUrl = "https://example.org/video.mp4"
	)
}
