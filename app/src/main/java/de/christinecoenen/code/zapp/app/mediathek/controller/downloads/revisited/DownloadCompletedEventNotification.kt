package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.app.NotificationManager
import android.content.Context
import de.christinecoenen.code.zapp.R

class DownloadCompletedEventNotification(
	appContext: Context,
	title: String,
	persistedShowId: Int
) : DownloadNotification(appContext, title, persistedShowId) {

	init {
		notificationBuilder
			.setPriority(NotificationManager.IMPORTANCE_DEFAULT)
			.setSmallIcon(R.drawable.ic_outline_check_24)
			.setAutoCancel(true)
			.setContentText(appContext.getString(R.string.notification_download_complete))
	}

}
