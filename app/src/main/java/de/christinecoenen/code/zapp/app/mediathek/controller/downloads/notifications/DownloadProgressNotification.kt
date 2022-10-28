package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import de.christinecoenen.code.zapp.R

class DownloadProgressNotification(
	appContext: Context,
	title: String,
	persistedShowId: Int,
	cancelIntent: PendingIntent
) : DownloadNotification(appContext, title, persistedShowId) {

	init {
		notificationBuilder
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

	fun build(progress: Int) = notificationBuilder
		.setProgress(100, progress, progress == 0)
		.build()
}
