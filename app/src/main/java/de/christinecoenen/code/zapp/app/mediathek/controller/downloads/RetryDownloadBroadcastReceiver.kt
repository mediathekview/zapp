package de.christinecoenen.code.zapp.app.mediathek.controller.downloads

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.christinecoenen.code.zapp.models.shows.Quality
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class RetryDownloadBroadcastReceiver : BroadcastReceiver(), KoinComponent {

	companion object {

		private const val EXTRA_DOWNLOAD_ID = "EXTRA_DOWNLOAD_ID"
		private const val EXTRA_DOWNLOAD_QUALITY = "EXTRA_DOWNLOAD_QUALITY"

		fun getPendingIntent(context: Context, downloadId: Int, quality: Quality): PendingIntent {
			val intent = Intent(context, RetryDownloadBroadcastReceiver::class.java)
			intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId)
			intent.putExtra(EXTRA_DOWNLOAD_QUALITY, quality)

			return PendingIntent.getBroadcast(
				context,
				0,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
			)
		}
	}

	private val downloadController: IDownloadController by inject()
	private val mediathekRepository: MediathekRepository by inject()

	override fun onReceive(context: Context?, intent: Intent?) {
		val downloadId = intent?.extras?.getInt(EXTRA_DOWNLOAD_ID)
		val quality = intent?.extras?.getSerializable(EXTRA_DOWNLOAD_QUALITY) as Quality?

		if (downloadId == null) {
			Timber.w("no download id set")
			return
		}

		if (quality == null) {
			Timber.w("no download quality set")
			return
		}

		MainScope().launch {
			val show = mediathekRepository.getPersistedShowByDownloadId(downloadId).firstOrNull()

			if (show == null) {
				Timber.w("show with download id $downloadId not found")
				return@launch
			}

			Timber.d("retry download: $downloadId")
			downloadController.startDownload(show.id, quality)
		}
	}

}
