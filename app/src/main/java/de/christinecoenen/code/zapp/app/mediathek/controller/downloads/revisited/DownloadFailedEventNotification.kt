package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.content.Context
import de.christinecoenen.code.zapp.R

class DownloadFailedEventNotification(
	appContext: Context,
	title: String
) : DownloadEventNotification(appContext, title) {

	// TODO: add retry action
	init {
		notificationBuilder
			.setSmallIcon(R.drawable.ic_warning_white_24dp)
			.setAutoCancel(true)
			.setContentText(appContext.getString(R.string.notification_download_failed))
	}

}
