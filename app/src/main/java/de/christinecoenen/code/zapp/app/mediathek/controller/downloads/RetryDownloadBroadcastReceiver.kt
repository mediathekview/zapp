package de.christinecoenen.code.zapp.app.mediathek.controller.downloads

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.christinecoenen.code.zapp.models.shows.Quality
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class RetryDownloadBroadcastReceiver : BroadcastReceiver(), KoinComponent {

	companion object {

		private const val EXTRA_PERSISTED_SHOW_ID = "EXTRA_PERSISTED_SHOW_ID"
		private const val EXTRA_DOWNLOAD_QUALITY = "EXTRA_DOWNLOAD_QUALITY"

		fun getPendingIntent(
			context: Context,
			persistedShowId: Int,
			quality: Quality
		): PendingIntent {
			val intent = Intent(context, RetryDownloadBroadcastReceiver::class.java)
			intent.putExtra(EXTRA_PERSISTED_SHOW_ID, persistedShowId)
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

	override fun onReceive(context: Context?, intent: Intent?) {
		val persistedShowId = intent?.extras?.getInt(EXTRA_PERSISTED_SHOW_ID)
		val quality = intent?.extras?.getSerializable(EXTRA_DOWNLOAD_QUALITY) as Quality?

		if (persistedShowId == null) {
			Timber.w("no persisted show id set")
			return
		}

		if (quality == null) {
			Timber.w("no download quality set")
			return
		}

		MainScope().launch {
			Timber.d("retry download: $persistedShowId")
			downloadController.startDownload(persistedShowId, quality)
		}
	}

}
