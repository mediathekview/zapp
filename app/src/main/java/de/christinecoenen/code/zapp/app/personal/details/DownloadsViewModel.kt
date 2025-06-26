package de.christinecoenen.code.zapp.app.personal.details

import de.christinecoenen.code.zapp.repositories.MediathekRepository

class DownloadsViewModel(
	private val mediathekRepository: MediathekRepository
) : DetailsBaseViewModel() {

	override fun getPagingSource() = mediathekRepository.getDownloads()
}
