package de.christinecoenen.code.zapp.app.mediathek.controller.downloads

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications.DownloadCompletedEventNotification
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications.DownloadFailedEventNotification
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications.DownloadProgressNotification
import de.christinecoenen.code.zapp.models.shows.Quality
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.Duration.Companion.milliseconds

class DownloadWorker(appContext: Context, workerParams: WorkerParameters) :
	CoroutineWorker(appContext, workerParams), KoinComponent {

	companion object {
		private const val PersistedShowIdKey = "PersistedShowId"
		private const val ProgressKey = "Progress"
		private const val SourceUrlKey = "SourceUrl"
		private const val TargetFileUriKey = "TargetFileUri"
		private const val TitleKey = "Title"
		private const val QualityKey = "Quality"
		private const val BufferSize = DEFAULT_BUFFER_SIZE
		private val NotificationDelay = 100.milliseconds

		fun constructInputData(
			persistedShowId: Int,
			sourceUrl: String,
			targetFileUri: String,
			title: String,
			quality: Quality
		) =
			workDataOf(
				PersistedShowIdKey to persistedShowId,
				SourceUrlKey to sourceUrl,
				TargetFileUriKey to targetFileUri,
				TitleKey to title,
				QualityKey to quality.name
			)

		/**
		 * @return download progress between 0 and 100
		 */
		fun getProgress(workInfo: WorkInfo) = workInfo.progress.getInt(ProgressKey, 0)
	}

	private val httpClient: OkHttpClient by inject()
	private val downloadFileInfoManager: DownloadFileInfoManager by inject()

	private val notificationManager = NotificationManagerCompat.from(applicationContext)

	private val persistedShowId by lazy { inputData.getInt(PersistedShowIdKey, -1) }
	private val sourceUrl by lazy { inputData.getString(SourceUrlKey) }
	private val targetFileUri by lazy { inputData.getString(TargetFileUriKey) }
	private val title by lazy { inputData.getString(TitleKey) ?: "" }
	private val quality by lazy { Quality.valueOf(inputData.getString(QualityKey)!!) }
	private val notificationId by lazy { id.hashCode() }
	private val downloadId by lazy { notificationId }

	private var progress = 0

	private val downloadProgressNotification = DownloadProgressNotification(
		appContext, title, persistedShowId, getCancelIntent(),
	)

	override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
		reportProgress()

		if (sourceUrl == null || targetFileUri == null || persistedShowId == -1) {
			return@withContext failure()
		}

		val request = Request.Builder().url(sourceUrl!!).build()
		val response = httpClient.newCall(request).execute()

		if (!response.isSuccessful || response.body() == null) {
			Timber.w("server response not successful")
			return@withContext failure()
		}

		val body = response.body()!!
		try {
			downloadFileInfoManager.openOutputStream(targetFileUri!!).use { outputSream ->
				if (outputSream == null) {
					Timber.w("fileoutputstream not readable")
					return@use failure()
				}

				body.byteStream().use { inputStream ->
					download(inputStream, outputSream, body.contentLength())
				}
			}
		} catch (e: CancellationException) {
			// cancelled - no not show any notification
			return@withContext Result.failure()
		} catch (e: Exception) {
			Timber.w(e)
			return@withContext failure()
		}

		progress = 100
		reportProgress()

		return@withContext success()
	}

	private fun success(): Result {
		MainScope().launch {
			delay(NotificationDelay)

			val notification = DownloadCompletedEventNotification(
				applicationContext,
				title,
				persistedShowId
			)
			notificationManager.notify(notificationId, notification.build())
		}

		return Result.success()
	}

	private fun failure(): Result {
		MainScope().launch {
			delay(NotificationDelay)

			val retryIntent = RetryDownloadBroadcastReceiver.getPendingIntent(
				applicationContext, downloadId, quality
			)

			val notification = DownloadFailedEventNotification(
				applicationContext,
				title,
				persistedShowId,
				retryIntent
			)
			notificationManager.notify(notificationId, notification.build())
		}

		return Result.failure()
	}

	private suspend fun download(
		inputStream: InputStream,
		outputStream: OutputStream,
		contentLength: Long
	) = withContext(Dispatchers.IO) {
		var bytesCopied = 0L
		var readCount = 0L
		val buffer = ByteArray(BufferSize)
		var bytes = inputStream.read(buffer)

		while (bytes >= 0 && !isStopped) {
			outputStream.write(buffer, 0, bytes)
			bytesCopied += bytes

			bytes = inputStream.read(buffer)
			readCount++

			// TODO: use a better, time based debounce
			if (readCount % 500 == 0L) {
				progress = ((bytesCopied * 100) / contentLength).toInt()
				reportProgress()
			}
		}
	}

	override suspend fun getForegroundInfo() =
		ForegroundInfo(id.hashCode(), downloadProgressNotification.build(progress))

	private suspend fun reportProgress() {
		val update = workDataOf(ProgressKey to progress)
		setProgress(update)

		setForeground(getForegroundInfo())
	}

	private fun getCancelIntent(): PendingIntent = WorkManager.getInstance(applicationContext)
		.createCancelPendingIntent(id)
}
