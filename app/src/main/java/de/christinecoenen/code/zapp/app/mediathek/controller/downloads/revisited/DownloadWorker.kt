package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DownloadWorker(appContext: Context, workerParams: WorkerParameters) :
	CoroutineWorker(appContext, workerParams), KoinComponent {

	private val httpClient: OkHttpClient by inject()

	companion object {
		private const val Progress = "Progress"
		private const val SourceUrl = "SourceUrl"
		private const val TargetFileUri = "TargetFileUri"

		fun constructInputData(sourceUrl: String, targetFileUri: String) = workDataOf(
			SourceUrl to sourceUrl,
			TargetFileUri to targetFileUri
		)

		fun getProgress(workInfo: WorkInfo) = workInfo.progress.getInt(Progress, 0)
	}

	override suspend fun doWork(): Result {
		// TODO: run in foreground

		reportProgress(0)

		val sourceUrl = inputData.getString(SourceUrl) ?: return Result.failure()
		val targetFileUri = inputData.getString(TargetFileUri) ?: return Result.failure()

		val request = Request.Builder().url(sourceUrl).build()
		val response = httpClient.newCall(request).execute()

		if (!response.isSuccessful || response.body() == null) {
			return Result.failure()
		}

		val fileOutputStream = applicationContext
			.contentResolver
			.openOutputStream(Uri.parse(targetFileUri)) ?: return Result.failure()
		val fileInputStream = response.body()!!.byteStream()

		try {
			// TODO: report progress
			fileInputStream.copyTo(fileOutputStream, 1000)
		} catch (e: Exception) {
			return Result.failure()
		} finally {
			fileOutputStream.close()
			fileInputStream.close()
		}

		reportProgress(100)

		return Result.success()
	}

	private suspend fun reportProgress(progress: Int) {
		val update = workDataOf(Progress to progress)
		setProgress(update)
	}
}
