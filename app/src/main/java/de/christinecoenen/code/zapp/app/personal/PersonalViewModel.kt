package de.christinecoenen.code.zapp.app.personal

import androidx.lifecycle.ViewModel
import de.christinecoenen.code.zapp.repositories.MediathekRepository


class PersonalViewModel(mediathekRepository: MediathekRepository) : ViewModel() {

	val downloadsFlow = mediathekRepository.getDownloads(3)

}
