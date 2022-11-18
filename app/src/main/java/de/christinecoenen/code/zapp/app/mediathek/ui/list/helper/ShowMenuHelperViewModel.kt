package de.christinecoenen.code.zapp.app.mediathek.ui.list.helper

import androidx.lifecycle.ViewModel
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.IDownloadController
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.flow.firstOrNull

class ShowMenuHelperViewModel(
	private val mediathekRepository: MediathekRepository,
	private val downloadController: IDownloadController
) : ViewModel() {

	suspend fun remove(show: MediathekShow) {
		mediathekRepository
			.getPersistedShowByApiId(show.apiId)
			.firstOrNull()
			?.let {
				downloadController.deleteDownload(it.id)
				mediathekRepository.updateDownloadStatus(it.downloadId, DownloadStatus.REMOVED)
			}
	}

}
