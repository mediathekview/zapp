package de.christinecoenen.code.zapp.app.mediathek.controller.downloads

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.asFlow
import androidx.work.*
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.DownloadException
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.NoNetworkException
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications.DownloadQueuedEventNotification
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications.DownloadQueuedForRetryEventNotification
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.system.NotificationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

/**
 * Implementation of IDownloadController using WorkManager for background execution.
 */
@OptIn(FlowPreview::class)
class WorkManagerDownloadController(
	val applicationContext: Context,
	private val scope: CoroutineScope,
	private val mediathekRepository: MediathekRepository,
	private val settingsRepository: SettingsRepository,
	private val downloadFileInfoManager: DownloadFileInfoManager
) : IDownloadController {

	private val workManager = WorkManager.getInstance(applicationContext)
	private val notificationManager = NotificationManagerCompat.from(applicationContext)
	private val connectivityManager =
		applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

	companion object {
		const val WorkTag = "zapp_download"
	}

	init {
		NotificationHelper.createDownloadProgressChannel(applicationContext)
		NotificationHelper.createDownloadEventChannel(applicationContext)

		scope.launch(Dispatchers.IO) {
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

	override suspend fun startDownload(persistedShowId: Int, quality: Quality) =
		withContext(Dispatchers.IO) {
			val show = mediathekRepository.getPersistedShow(persistedShowId).first()

			val downloadUrl = show.mediathekShow.getVideoUrl(quality)
				?: throw DownloadException("$quality is no valid download quality.")

			deleteDownload(show)

			val filePathUri =
				downloadFileInfoManager.getDownloadFilePath(show.mediathekShow, quality)

			val networkType = if (settingsRepository.downloadOverUnmeteredNetworkOnly)
				NetworkType.UNMETERED else NetworkType.CONNECTED

			if (connectivityManager.activeNetwork == null) {
				throw NoNetworkException("No active network available.")
			}
			if (settingsRepository.downloadOverUnmeteredNetworkOnly && connectivityManager.isActiveNetworkMetered) {
				throw WrongNetworkConditionException("Download over metered networks prohibited.")
			}

			val constraints = Constraints.Builder()
				.setRequiresStorageNotLow(true)
				.setRequiredNetworkType(networkType)
				.build()

			val workerInput = DownloadWorker.constructInputData(
				persistedShowId,
				downloadUrl,
				filePathUri,
				show.mediathekShow.title,
				quality
			)

			val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
				.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.setConstraints(constraints)
				.setInputData(workerInput)
				.setBackoffCriteria(
					BackoffPolicy.LINEAR,
					OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
					TimeUnit.MILLISECONDS
				)
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

			Unit
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

	private suspend fun deleteDownload(show: PersistedMediathekShow) = withContext(Dispatchers.IO) {
		deleteFile(show)

		notificationManager.cancel(show.downloadId)

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

	private suspend fun updateWorkInDatabase(workInfo: WorkInfo) = withContext(Dispatchers.IO) {
		val show = mediathekRepository
			.getPersistedShowByDownloadId(workInfo.id.hashCode())
			.firstOrNull() ?: return@withContext

		show.downloadProgress = DownloadWorker.getProgress(workInfo)

		show.downloadStatus = when (workInfo.state) {
			WorkInfo.State.SUCCEEDED -> DownloadStatus.COMPLETED
			WorkInfo.State.ENQUEUED -> DownloadStatus.QUEUED
			WorkInfo.State.BLOCKED -> DownloadStatus.QUEUED
			WorkInfo.State.RUNNING -> DownloadStatus.DOWNLOADING
			WorkInfo.State.FAILED -> DownloadStatus.FAILED
			WorkInfo.State.CANCELLED -> DownloadStatus.CANCELLED
		}

		mediathekRepository.updateShow(show)

		showStatusChangeNotificationIfNeeded(workInfo, show)
		deleteFileOnStatusChangeIfNeeded(show)
		updateMediaCollectionOnStatusChangeIfNeeded(show)
	}

	private fun updateMediaCollectionOnStatusChangeIfNeeded(show: PersistedMediathekShow) {
		if (show.downloadedVideoPath == null) {
			return
		}

		val fileUri = Uri.parse(show.downloadedVideoPath)

		downloadFileInfoManager.updateDownloadFileInMediaCollection(
			fileUri,
			show.downloadStatus
		)
	}

	private suspend fun deleteFileOnStatusChangeIfNeeded(show: PersistedMediathekShow) =
		withContext(Dispatchers.IO) {
			when (show.downloadStatus) {
				DownloadStatus.FAILED,
				DownloadStatus.CANCELLED -> {
					deleteFile(show)
				}
				else -> {}
			}
		}

	@SuppressLint("MissingPermission")
	private fun showStatusChangeNotificationIfNeeded(
		workInfo: WorkInfo,
		show: PersistedMediathekShow
	) {
		if (!notificationManager.areNotificationsEnabled() ||
			!NotificationHelper.hasNotificationPermissionGranted(applicationContext)
		) {
			return
		}

		val notificationTitle = show.mediathekShow.title
		val notification = when (show.downloadStatus) {
			DownloadStatus.QUEUED -> {
				val cancelIntent = workManager.createCancelPendingIntent(workInfo.id)

				if (workInfo.runAttemptCount == 0) {
					DownloadQueuedEventNotification(
						applicationContext,
						notificationTitle,
						show.id,
						cancelIntent
					)
				} else {
					DownloadQueuedForRetryEventNotification(
						applicationContext,
						notificationTitle,
						show.id,
						workInfo.runAttemptCount,
						cancelIntent
					)
				}
			}
			DownloadStatus.CANCELLED -> {
				notificationManager.cancel(show.downloadId)
				null
			}
			DownloadStatus.FAILED,
			DownloadStatus.COMPLETED -> {
				// will be handled in worker
				null
			}
			else -> {
				// no notification needed
				null
			}
		}

		notification?.let {
			notificationManager.notify(show.downloadId, it.build())
		}
	}

	private suspend fun deleteFile(mediathekShow: PersistedMediathekShow) =
		withContext(Dispatchers.IO) {
			mediathekShow.downloadedVideoPath?.let {
				downloadFileInfoManager.deleteDownloadFile(it)
			}

			mediathekRepository.updateDownloadedVideoPath(mediathekShow.downloadId, null)
			mediathekRepository.updateDownloadProgress(mediathekShow.downloadId, 0)
		}
}
