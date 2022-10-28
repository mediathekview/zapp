package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import de.christinecoenen.code.zapp.R

class DownloadFailedEventNotification(
	appContext: Context,
	title: String,
	persistedShowId: Int,
	retryIntent: PendingIntent
) : DownloadNotification(appContext, title, persistedShowId) {

	init {
		notificationBuilder
			.setPriority(NotificationManager.IMPORTANCE_DEFAULT)
			.setSmallIcon(R.drawable.ic_warning_white_24dp)
			.setAutoCancel(true)
			.setContentText(appContext.getString(R.string.notification_download_failed))
			.addAction(
				R.drawable.ic_refresh_white_24dp,
				appContext.getString(R.string.menu_retry),
				retryIntent
			)
	}

}
