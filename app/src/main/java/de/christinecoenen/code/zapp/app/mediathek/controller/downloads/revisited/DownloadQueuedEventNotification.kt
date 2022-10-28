package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.app.PendingIntent
import android.content.Context
import de.christinecoenen.code.zapp.R

class DownloadQueuedEventNotification(
	appContext: Context,
	title: String,
	cancelIntent: PendingIntent
) : DownloadEventNotification(appContext, title) {

	// TODO: explain in text what "queued" means
	init {
		notificationBuilder
			.setOngoing(true)
			.setSilent(true)
			.setProgress(0, 0, true)
			.setContentText(appContext.getString(R.string.fetch_notification_download_starting))
			.addAction(
				R.drawable.fetch_notification_cancel,
				appContext.getString(R.string.fetch_notification_download_cancel),
				cancelIntent
			)
	}

}
