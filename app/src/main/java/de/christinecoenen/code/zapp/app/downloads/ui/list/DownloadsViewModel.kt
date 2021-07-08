package de.christinecoenen.code.zapp.app.downloads.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.liveData
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import io.reactivex.Flowable


class DownloadsViewModel(private val mediathekRepository: MediathekRepository) : ViewModel() {

	private val pagingConfig = PagingConfig(pageSize = 20)

	val downloadList = Pager(pagingConfig) { mediathekRepository.downloads }
		.liveData
		.cachedIn(viewModelScope)

	fun getPersistedShowFlowable(id: Int): Flowable<PersistedMediathekShow> {
		return mediathekRepository.getPersistedShow(id)
	}

}
