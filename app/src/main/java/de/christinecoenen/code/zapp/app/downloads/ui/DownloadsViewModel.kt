package de.christinecoenen.code.zapp.app.downloads.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekRepository
import io.reactivex.Flowable


class DownloadsViewModel(private val mediathekRepository: MediathekRepository) : ViewModel() {

	val downloadList: LiveData<PagedList<PersistedMediathekShow>> =
		LivePagedListBuilder(mediathekRepository.downloads, 20)
			.build()

	fun getPersistedShowFlowable(id: Int): Flowable<PersistedMediathekShow> {
		return mediathekRepository.getPersistedShow(id)
	}

}
