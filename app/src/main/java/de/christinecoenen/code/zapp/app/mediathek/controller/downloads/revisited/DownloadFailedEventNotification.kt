package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.content.Context

class DownloadFailedEventNotification(
	appContext: Context,
	title: String
) : DownloadEventNotification(appContext, title) {

	// TODO: add retry action
	init {
		notificationBuilder
			.setAutoCancel(true)
			.setContentText("FAILED!")
	}

}
