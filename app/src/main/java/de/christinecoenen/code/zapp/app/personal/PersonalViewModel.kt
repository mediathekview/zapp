package de.christinecoenen.code.zapp.app.personal

import androidx.lifecycle.ViewModel
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map


class PersonalViewModel(mediathekRepository: MediathekRepository) : ViewModel() {

	val downloadsFlow = mediathekRepository.getDownloads(2)
	val continueWatchingFlow = mediathekRepository.getStarted(2)
	val bookmarkFlow = mediathekRepository.getBookmarked(2)

}
