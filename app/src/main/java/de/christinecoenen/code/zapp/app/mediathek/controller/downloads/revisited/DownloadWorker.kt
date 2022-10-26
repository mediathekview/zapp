package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import androidx.work.*
import de.christinecoenen.code.zapp.utils.system.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.InputStream
import java.io.OutputStream

class DownloadWorker(appContext: Context, workerParams: WorkerParameters) :
	CoroutineWorker(appContext, workerParams), KoinComponent {

	companion object {
		private const val ProgressKey = "Progress"
		private const val SourceUrlKey = "SourceUrl"
		private const val TargetFileUriKey = "TargetFileUri"
		private const val TitleKey = "Title"
		private const val BufferSize = DEFAULT_BUFFER_SIZE

		fun constructInputData(sourceUrl: String, targetFileUri: String, title: String) =
			workDataOf(
				SourceUrlKey to sourceUrl,
				TargetFileUriKey to targetFileUri,
				TitleKey to title
			)

		/**
		 * @return download progress between 0 and 100
		 */
		fun getProgress(workInfo: WorkInfo) = workInfo.progress.getInt(ProgressKey, 0)
	}

	private val httpClient: OkHttpClient by inject()

	private val sourceUrl by lazy { inputData.getString(SourceUrlKey) }
	private val targetFileUri by lazy { inputData.getString(TargetFileUriKey) }
	private val title by lazy { inputData.getString(TitleKey) ?: "" }

	private val downloadProgressNotification = DownloadProgressNotification(
		appContext, title, getCancelIntent()
	)

	init {
		NotificationHelper.createDownloadProgressChannel(applicationContext)
	}

	override suspend fun doWork(): Result {
		reportProgress(0)

		if (sourceUrl == null || targetFileUri == null) {
			return Result.failure()
		}

		val request = Request.Builder().url(sourceUrl!!).build()
		val response = httpClient.newCall(request).execute()

		if (!response.isSuccessful || response.body() == null) {
			return Result.failure()
		}

		val fileOutputStream = applicationContext
			.contentResolver
			.openOutputStream(Uri.parse(targetFileUri)) ?: return Result.failure()
		val body = response.body()!!
		val fileInputStream = body.byteStream()

		try {
			download(fileInputStream, fileOutputStream, body.contentLength())
		} catch (e: Exception) {
			return Result.failure()
		} finally {
			withContext(Dispatchers.IO) {
				fileOutputStream.close()
				fileInputStream.close()
			}
		}

		reportProgress(100)

		return Result.success()
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
				val progress = ((bytesCopied * 100) / contentLength).toInt()
				reportProgress(progress)
			}
		}
	}

	private fun createForegroundInfo(progress: Int) =
		ForegroundInfo(id.hashCode(), downloadProgressNotification.build(progress))

	private suspend fun reportProgress(progress: Int) {
		val update = workDataOf(ProgressKey to progress)
		setProgress(update)

		setForeground(createForegroundInfo(progress))
	}

	private fun getCancelIntent(): PendingIntent = WorkManager.getInstance(applicationContext)
		.createCancelPendingIntent(id)
}
