package de.christinecoenen.code.zapp.app.mediathek.controller.downloads

import android.content.Context
import android.net.Uri
import androidx.lifecycle.asFlow
import androidx.work.*
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.DownloadException
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited.DownloadWorker
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import kotlin.time.Duration.Companion.milliseconds

/**
 * Implementation of IDownloadController using WorkManager for background execution.
 */
@OptIn(FlowPreview::class)
class WorkManagerDownloadController(
	val applicationContext: Context,
	private val scope: CoroutineScope,
	private val mediathekRepository: MediathekRepository,
	settingsRepository: SettingsRepository
) : IDownloadController {

	private val downloadFileInfoManager: DownloadFileInfoManager =
		DownloadFileInfoManager(applicationContext, settingsRepository)

	private val workManager = WorkManager.getInstance(applicationContext)

	companion object {
		const val WorkTag = "zapp_download"
	}

	init {
		workManager.pruneWork()
		scope.launch {
			workManager
				.getWorkInfosByTagLiveData(WorkTag)
				.asFlow()
				.debounce(250.milliseconds)
				.collectLatest { workInfos ->
					workInfos.onEach {
						updateWorkInDatabase(it)
					}
				}
		}
	}

	override suspend fun startDownload(show: PersistedMediathekShow, quality: Quality) {
		val downloadUrl = show.mediathekShow.getVideoUrl(quality)
			?: throw DownloadException("$quality is no valid download quality.")

		val filePathUri =
			downloadFileInfoManager.getDownloadFilePath(show.mediathekShow, quality)

		// TODO: delete any downloads with wrong quality
		// TODO: set wifi constraints

		val workerInput = DownloadWorker.constructInputData(
			downloadUrl,
			filePathUri,
			show.mediathekShow.title
		)
		val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
			.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
			.setInputData(workerInput)
			.addTag(show.id.toString())
			.addTag(WorkTag)
			.build()

		show.downloadId = downloadWorkRequest.id.hashCode()
		show.downloadedVideoPath = filePathUri
		show.downloadedAt = DateTime.now()
		show.downloadProgress = 0

		mediathekRepository.updateShow(show)

		workManager.enqueueUniqueWork(
			downloadUrl,
			ExistingWorkPolicy.KEEP,
			downloadWorkRequest
		)
	}

	override fun stopDownload(persistedShowId: Int) {
		deleteDownload(persistedShowId)
	}

	override fun deleteDownload(persistedShowId: Int) {
		workManager.cancelAllWorkByTag(persistedShowId.toString())

		scope.launch {
			val show = mediathekRepository
				.getPersistedShow(persistedShowId)
				.firstOrNull() ?: return@launch

			deleteDownload(show)
		}
	}

	private suspend fun deleteDownload(show: PersistedMediathekShow) {
		deleteFile(show)

		show.downloadProgress = 0
		show.downloadStatus = DownloadStatus.NONE
		show.downloadId = 0
		show.downloadedAt = null
		show.downloadedVideoPath = null

		mediathekRepository.updateShow(show)
	}

	override fun deleteDownloadsWithDeletedFiles() {
		scope.launch {
			mediathekRepository
				.getCompletedDownloads()
				.first()
				.forEach {
					if (downloadFileInfoManager.shouldDeleteDownload(it)) {
						deleteDownload(it)
					}
				}
		}
	}

	override fun getDownloadStatus(persistedShowId: Int): Flow<DownloadStatus> {
		return mediathekRepository.getDownloadStatus(persistedShowId)
	}

	override fun getDownloadProgress(persistedShowId: Int): Flow<Int> {
		return mediathekRepository.getDownloadProgress(persistedShowId)
	}

	private suspend fun updateWorkInDatabase(workInfo: WorkInfo) {
		val show = mediathekRepository
			.getPersistedShowByDownloadId(workInfo.id.hashCode())
			.firstOrNull() ?: return

		show.downloadProgress = DownloadWorker.getProgress(workInfo)

		show.downloadStatus = when (workInfo.state) {
			WorkInfo.State.SUCCEEDED -> DownloadStatus.COMPLETED
			WorkInfo.State.ENQUEUED -> DownloadStatus.QUEUED
			WorkInfo.State.RUNNING -> DownloadStatus.DOWNLOADING
			WorkInfo.State.FAILED -> DownloadStatus.FAILED
			WorkInfo.State.BLOCKED -> DownloadStatus.ADDED
			WorkInfo.State.CANCELLED -> DownloadStatus.CANCELLED
		}

		mediathekRepository.updateShow(show)

		// TODO: show notification on error or success

		when (workInfo.state) {
			WorkInfo.State.FAILED,
			WorkInfo.State.CANCELLED -> {
				deleteFile(show)
			}
			else -> {}
		}

		if (show.downloadedVideoPath != null) {
			val fileUri = Uri.parse(show.downloadedVideoPath)

			downloadFileInfoManager.updateDownloadFileInMediaCollection(
				fileUri,
				show.downloadStatus
			)
		}
	}

	private suspend fun deleteFile(mediathekShow: PersistedMediathekShow) {
		mediathekShow.downloadedVideoPath?.let {
			downloadFileInfoManager.deleteDownloadFile(it)
		}

		mediathekRepository.updateDownloadedVideoPath(mediathekShow.downloadId, null)
		mediathekRepository.updateDownloadProgress(mediathekShow.downloadId, 0)
	}
}
