package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.app.NotificationManager
import android.content.Context

class DownloadCompletedEventNotification(
	appContext: Context,
	title: String
) : DownloadEventNotification(appContext, title) {

	init {
		notificationBuilder
			.setPriority(NotificationManager.IMPORTANCE_DEFAULT)
			.setAutoCancel(true)
			.setContentText("COMPLETED!")
	}

}
