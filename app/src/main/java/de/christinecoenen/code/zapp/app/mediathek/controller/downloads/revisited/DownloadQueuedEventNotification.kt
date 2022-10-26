package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.content.Context
import de.christinecoenen.code.zapp.R

class DownloadQueuedEventNotification(
	appContext: Context,
	title: String
) : DownloadEventNotification(appContext, title) {

	// TODO: add cancel action
	// TODO: explain in text what "queued" means
	init {
		notificationBuilder
			.setOngoing(true)
			.setSilent(true)
			.setProgress(0, 0, true)
			.setContentText(appContext.getString(R.string.fetch_notification_download_starting))
	}

}
