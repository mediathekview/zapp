package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.text.format.Formatter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.NotificationHelper

class DownloadProgressNotification(
	private val appContext: Context,
	title: String,
	persistedShowId: Int,
	cancelIntent: PendingIntent
) : DownloadNotification(appContext, title, persistedShowId) {

	init {
		notificationBuilder
			.setChannelId(NotificationHelper.CHANNEL_ID_DOWNLOAD_PROGRESS)
			.setContentText(appContext.getString(R.string.notification_download_downloading))
			.setPriority(NotificationManager.IMPORTANCE_MIN)
			.setOngoing(true)
			.setSilent(true)
			.setSmallIcon(android.R.drawable.stat_sys_download)
			.addAction(
				R.drawable.ic_baseline_close_24,
				appContext.getString(R.string.action_cancel),
				cancelIntent
			)
	}

	fun build(progress: Int, downloadedBytes: Long, totalBytes: Long): Notification {

		if (downloadedBytes != 0L && totalBytes != 0L) {
			val downloaded = Formatter.formatShortFileSize(appContext, downloadedBytes)
			val total = Formatter.formatShortFileSize(appContext, totalBytes)

			notificationBuilder.setSubText(
				appContext.getString(
					R.string.notification_download_downloading_size,
					downloaded,
					total
				)
			)
		} else {
			notificationBuilder.setSubText(null)
		}

		return notificationBuilder
			.setProgress(100, progress, downloadedBytes == 0L)
			.build()
	}
}
