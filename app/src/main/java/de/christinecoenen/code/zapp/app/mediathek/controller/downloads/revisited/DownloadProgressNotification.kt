package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.NotificationHelper

class DownloadProgressNotification(
	appContext: Context,
	title: String,
	cancelIntent: PendingIntent
) {

	// TODO: make notification clickable
	private val progressNotificationBuilder = NotificationCompat.Builder(
		appContext,
		NotificationHelper.CHANNEL_ID_DOWNLOAD_PROGRESS
	)
		.setContentTitle(title)
		.setTicker(title)
		.setContentText(appContext.getString(R.string.fetch_notification_download_downloading))
		.setOngoing(true)
		.setSmallIcon(R.drawable.ic_baseline_save_alt_24)
		.setPriority(NotificationManager.IMPORTANCE_MIN)
		.setCategory(Notification.CATEGORY_SERVICE)
		.addAction(
			R.drawable.fetch_notification_cancel,
			appContext.getString(R.string.fetch_notification_download_cancel),
			cancelIntent
		)

	fun build(progress: Int) = progressNotificationBuilder
		.setProgress(100, progress, progress == 0)
		.build()
}
