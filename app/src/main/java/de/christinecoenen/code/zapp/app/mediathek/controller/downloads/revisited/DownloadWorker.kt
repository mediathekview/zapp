package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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

	private val httpClient: OkHttpClient by inject()

	companion object {
		private const val ProgressKey = "Progress"
		private const val SourceUrlKey = "SourceUrl"
		private const val TargetFileUriKey = "TargetFileUri"
		private const val BufferSize = 1024

		fun constructInputData(sourceUrl: String, targetFileUri: String) = workDataOf(
			SourceUrlKey to sourceUrl,
			TargetFileUriKey to targetFileUri
		)

		/**
		 * @return download progress between 0 and 100
		 */
		fun getProgress(workInfo: WorkInfo) = workInfo.progress.getInt(ProgressKey, 0)
	}

	override suspend fun doWork(): Result {
		// TODO: run in foreground

		reportProgress(0)

		val sourceUrl = inputData.getString(SourceUrlKey) ?: return Result.failure()
		val targetFileUri = inputData.getString(TargetFileUriKey) ?: return Result.failure()

		val request = Request.Builder().url(sourceUrl).build()
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
		var bytesCopied = 0
		val buffer = ByteArray(BufferSize)
		var bytes = inputStream.read(buffer)

		while (bytes >= 0 && !isStopped) {
			outputStream.write(buffer, 0, bytes)
			bytesCopied += bytes

			bytes = inputStream.read(buffer)

			val progress = ((bytesCopied * 100) / contentLength).toInt()
			reportProgress(progress)
		}
	}

	private suspend fun reportProgress(progress: Int) {
		val update = workDataOf(ProgressKey to progress)
		setProgress(update)
	}
}
