package de.christinecoenen.code.zapp.app.personal.details

import de.christinecoenen.code.zapp.repositories.MediathekRepository

class BookmarksViewModel(
	private val mediathekRepository: MediathekRepository
) : DetailsBaseViewModel() {

	override fun getPagingSource(searchQuery: String) =
		mediathekRepository.getBookmarked(searchQuery)
}
