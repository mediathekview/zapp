package de.christinecoenen.code.zapp.models.collections

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import org.joda.time.DateTime

/**
 * A named collection of shows that is defined by a mediathek search query.
 *
 * Whenever the collection is opened, the stored [searchQuery] is used to
 * search the mediatheks and the results are shown to the user.
 */
@Entity
data class ShowCollection(

	@PrimaryKey(autoGenerate = true)
	var id: Int = 0,

	var name: String,

	var searchQuery: String,

	/**
	 * Comma separated terms. Shows matching one of these terms are excluded from
	 * the collection and will not be shown.
	 */
	var excludeTerms: String = "",

	/**
	 * Whether season and episode numbers should be read from the show titles.
	 */
	var detectEpisodes: Boolean = false,

	/**
	 * Whether the shows should be sorted by season and episode instead of date.
	 */
	var sortByEpisodes: Boolean = false,

	/**
	 * Total number of shows found for [searchQuery] in the mediatheks.
	 */
	var totalCount: Int = 0,

	/**
	 * Number of found shows that have not been watched yet.
	 */
	var unwatchedCount: Int = 0,

	/**
	 * Time the cached counts have been calculated for the last time - null if never.
	 */
	var countUpdatedAt: DateTime? = null,

	/**
	 * Timestamp (seconds) of the newest show that was seen for this collection. Used to
	 * detect new shows. 0 means "not checked yet".
	 */
	var lastKnownShowTimestamp: Int = 0,

	var createdAt: DateTime = DateTime.now()
)

/**
 * The single exclusion terms contained in [ShowCollection.excludeTerms].
 */
val ShowCollection.excludedSearchTerms: List<String>
	get() = excludeTerms
		.split(',', ';', '\n')
		.map { it.trim() }
		.filter { it.isNotEmpty() }

/**
 * Whether the given show matches one of the collection's exclusion terms and
 * should therefore not be shown.
 */
fun ShowCollection.isExcluded(show: MediathekShow): Boolean {
	return excludedSearchTerms.any { term ->
		show.title.contains(term, ignoreCase = true) ||
			show.topic.contains(term, ignoreCase = true)
	}
}
