package de.christinecoenen.code.zapp.models.shows

/**
 * Season and episode number of a show, as far as they are known.
 */
data class EpisodeNumber(
	val season: Int?,
	val episode: Int?,
)

/**
 * Episode number of this show.
 *
 * Information provided by the mediathek is preferred. At the moment the mediathek does not
 * deliver season/episode, so the number is usually read from the title.
 *
 * Known title formats (the last parenthesis of a show title normally contains the number):
 *
 * * `(S20/E07)`, `(S20E7)`, `(Staffel 20, Folge 7)`
 * * `(20x07)`
 * * `(3/12)` - season and episode of that season
 * * `(Folge 7)`, `(Episode 7)`, `(Teil 7)`
 * * `(123)` - episode only
 */
fun MediathekShow.episodeNumber(): EpisodeNumber? {
	if (season != null || episode != null) {
		return EpisodeNumber(season, episode)
	}

	for (match in episodeContainers.findAll(title)) {
		parseEpisodeNumber(match.groupValues[1].trim())?.let { return it }
	}

	return null
}

/**
 * Sorts shows by season and episode - by default from the first to the last episode.
 *
 * Shows without a detected episode number always come last, the newest one first.
 */
class EpisodeComparator(
	private val descending: Boolean = false,
) : Comparator<MediathekShow> {

	override fun compare(first: MediathekShow, second: MediathekShow): Int {
		val firstEpisode = first.episodeNumber()
		val secondEpisode = second.episodeNumber()

		if (firstEpisode == null || secondEpisode == null) {
			return when {
				firstEpisode == null && secondEpisode == null -> second.timestamp - first.timestamp
				firstEpisode == null -> 1
				else -> -1
			}
		}

		val seasonComparison = compareValues(firstEpisode.season ?: 0, secondEpisode.season ?: 0)
		if (seasonComparison != 0) {
			return if (descending) -seasonComparison else seasonComparison
		}

		val episodeComparison = compareValues(firstEpisode.episode ?: 0, secondEpisode.episode ?: 0)
		if (episodeComparison != 0) {
			return if (descending) -episodeComparison else episodeComparison
		}

		return second.timestamp - first.timestamp
	}
}

/**
 * Text in parenthesis or brackets, e.g. "(S20/E07)" or "[Folge 3]".
 */
private val episodeContainers = Regex("""[(\[]([^()\[\]]*)[)\]]""")

/**
 * "S20/E07", "S20E7", "Staffel 20, Folge 7"
 */
private val seasonAndEpisode = Regex(
	"""(?:staffel|s)\.?\s*(\d{1,2})\s*[^\d]{0,6}?(?:folge|episode|ep|teil|e|f)\.?\s*(\d{1,3})""",
	RegexOption.IGNORE_CASE
)

/**
 * "20x07"
 */
private val seasonTimesEpisode = Regex("""^(\d{1,2})\s*[x×]\s*(\d{1,3})$""", RegexOption.IGNORE_CASE)

/**
 * "3/12" - season and episode of that season
 */
private val seasonSlashEpisode = Regex("""^(\d{1,2})\s*/\s*(\d{1,3})$""")

/**
 * "Folge 7", "Episode 7", "Teil 7", "E7", "F7"
 */
private val episodeOnly = Regex(
	"""(?:folge|episode|ep|teil|e|f)\.?\s*(\d{1,3})""",
	RegexOption.IGNORE_CASE
)

/**
 * "123" - episode only
 */
private val episodeOnlyNumber = Regex("""^(\d{1,3})$""")

/**
 * "Staffel 7"
 */
private val seasonOnly = Regex("""^(?:staffel|s)\.?\s*(\d{1,2})$""", RegexOption.IGNORE_CASE)

private fun parseEpisodeNumber(text: String): EpisodeNumber? {
	seasonAndEpisode.find(text)?.let { match ->
		return EpisodeNumber(
			season = match.groupValues[1].toIntOrNull(),
			episode = match.groupValues[2].toIntOrNull()
		)
	}

	seasonTimesEpisode.find(text)?.let { match ->
		return EpisodeNumber(
			season = match.groupValues[1].toIntOrNull(),
			episode = match.groupValues[2].toIntOrNull()
		)
	}

	seasonSlashEpisode.find(text)?.let { match ->
		return EpisodeNumber(
			season = match.groupValues[1].toIntOrNull(),
			episode = match.groupValues[2].toIntOrNull()
		)
	}

	episodeOnly.find(text)?.let { match ->
		return EpisodeNumber(season = null, episode = match.groupValues[1].toIntOrNull())
	}

	episodeOnlyNumber.find(text)?.let { match ->
		return EpisodeNumber(season = null, episode = match.groupValues[1].toIntOrNull())
	}

	seasonOnly.find(text)?.let { match ->
		return EpisodeNumber(season = match.groupValues[1].toIntOrNull(), episode = null)
	}

	return null
}
