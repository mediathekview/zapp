package de.christinecoenen.code.zapp.app.downloads.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow
import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekRepository


class DownloadsViewModel(mediathekRepository: MediathekRepository) : ViewModel() {

	val downloadList: LiveData<PagedList<PersistedMediathekShow>> =
		LivePagedListBuilder(mediathekRepository.downloads, 20)
			.build()

}
