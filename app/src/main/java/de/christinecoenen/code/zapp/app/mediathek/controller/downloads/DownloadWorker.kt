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
import java.net.HttpURLConnection
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
		private const val SupportRangeHeaderForRetries = true
		private const val MaxRetries = 3
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
	private var downloadedBytes = 0L
	private var totalBytes = 0L
	private var existingFileSize = 0L
	private var shouldResume = false

	private val downloadProgressNotification = DownloadProgressNotification(
		appContext, title, persistedShowId, getCancelIntent(),
	)

	override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
		reportProgress()

		if (sourceUrl == null || targetFileUri == null || persistedShowId == -1) {
			return@withContext failure(ErrorType.InitializationFailed)
		}

		if (SupportRangeHeaderForRetries) {
			existingFileSize = downloadFileInfoManager.getFileSize(targetFileUri!!)
			shouldResume = existingFileSize > 0
		}

		val request = Request.Builder()
			.url(sourceUrl!!)

		if (shouldResume) {
			request.header("Range", "bytes=$existingFileSize-")
		}

		val response = try {
			httpClient.newCall(request.build()).execute()
		} catch (e: Exception) {
			Timber.w("could not connect to server")
			return@withContext retry(ErrorType.FileReadFailed)
		}

		if (!response.isSuccessful) {
			Timber.w("server response not successful - response code: %s", response.code())
			return@withContext failure(response.code().toErrorType())
		}

		if (response.body() == null) {
			Timber.w("server response was empty")
			return@withContext failure(ErrorType.Unknown)
		}

		val body = response.body()!!

		if (response.code() == HttpURLConnection.HTTP_PARTIAL) {
			// server does support ranges
			downloadedBytes = existingFileSize
			totalBytes = existingFileSize + body.contentLength()
		} else {
			// server does not support range header - download regularly
			shouldResume = false
			totalBytes = body.contentLength()
		}

		val outputStream = try {
			downloadFileInfoManager.openOutputStream(targetFileUri!!, shouldResume)
		} catch (e: Exception) {
			return@withContext failure(ErrorType.FileWriteFailed)
		}

		if (outputStream == null) {
			Timber.w("fileoutputstream not readable")
			return@withContext failure(ErrorType.FileWriteFailed)
		}

		try {
			outputStream.use {
				body.byteStream().use { inputStream ->
					download(inputStream, outputStream)
				}
			}
		} catch (e: CancellationException) {
			// cancelled - no not show any notification
			return@withContext Result.failure()
		} catch (e: Exception) {
			// this is most likely a connection issue we can recover from - so we retry
			Timber.w(e)
			return@withContext retry(ErrorType.FileReadFailed)
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

	private fun failure(errorType: ErrorType): Result {
		MainScope().launch {
			delay(NotificationDelay)

			val retryIntent = RetryDownloadBroadcastReceiver.getPendingIntent(
				applicationContext, downloadId, quality
			)

			val notification = DownloadFailedEventNotification(
				applicationContext,
				title,
				persistedShowId,
				errorType,
				retryIntent
			)
			notificationManager.notify(notificationId, notification.build())
		}

		return Result.failure()
	}

	private fun retry(errorType: ErrorType) = if (runAttemptCount < MaxRetries) {
		Result.retry()
	} else {
		failure(errorType)
	}

	private suspend fun download(
		inputStream: InputStream,
		outputStream: OutputStream
	) = withContext(Dispatchers.IO) {
		var readCount = 0L
		val buffer = ByteArray(BufferSize)
		var bytes = inputStream.read(buffer)

		while (bytes >= 0 && !isStopped) {
			outputStream.write(buffer, 0, bytes)
			downloadedBytes += bytes

			bytes = inputStream.read(buffer)
			readCount++

			// TODO: use a better, time based debounce
			if (readCount % 500 == 0L) {
				progress = ((downloadedBytes * 100) / totalBytes).toInt()
				reportProgress()
			}
		}
	}

	override suspend fun getForegroundInfo() =
		ForegroundInfo(
			id.hashCode(),
			downloadProgressNotification.build(progress, downloadedBytes, totalBytes)
		)

	private suspend fun reportProgress() {
		val update = workDataOf(ProgressKey to progress)
		setProgress(update)

		setForeground(getForegroundInfo())
	}

	private fun getCancelIntent(): PendingIntent = WorkManager.getInstance(applicationContext)
		.createCancelPendingIntent(id)

	private fun Int.toErrorType(): ErrorType = when (this) {
		HttpURLConnection.HTTP_NOT_FOUND,
		HttpURLConnection.HTTP_GONE ->
			ErrorType.FileNotFound
		HttpURLConnection.HTTP_UNAUTHORIZED,
		HttpURLConnection.HTTP_FORBIDDEN,
		451 ->
			ErrorType.FileForbidden
		429 ->
			ErrorType.TooManyRequests
		in 400..499 ->
			ErrorType.ClientError
		in 500..600 ->
			ErrorType.ServerError
		else ->
			ErrorType.Unknown
	}
}
