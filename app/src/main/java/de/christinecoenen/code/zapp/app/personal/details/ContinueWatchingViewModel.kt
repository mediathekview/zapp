package de.christinecoenen.code.zapp.app.personal.details

import de.christinecoenen.code.zapp.repositories.MediathekRepository

class ContinueWatchingViewModel(
	private val mediathekRepository: MediathekRepository
) : DetailsBaseViewModel() {

	override fun getPagingSource() = mediathekRepository.getStarted()
}
