package de.christinecoenen.code.zapp.app.personal.details

import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.IDownloadController
import de.christinecoenen.code.zapp.repositories.MediathekRepository

class BookmarksViewModel(
	private val mediathekRepository: MediathekRepository,
	downloadController: IDownloadController
) : DetailsBaseViewModel(mediathekRepository, downloadController) {

	override fun getPagingSource(searchQuery: String) =
		mediathekRepository.getBookmarked(searchQuery)
}
