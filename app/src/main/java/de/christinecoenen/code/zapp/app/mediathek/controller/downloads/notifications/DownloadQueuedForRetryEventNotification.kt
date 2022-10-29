package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications

import android.app.PendingIntent
import android.content.Context
import de.christinecoenen.code.zapp.R

class DownloadQueuedForRetryEventNotification(
	appContext: Context,
	title: String,
	persistedShowId: Int,
	attemptCount: Int,
	cancelIntent: PendingIntent
) : DownloadQueuedEventNotification(appContext, title, persistedShowId, cancelIntent) {

	init {
		notificationBuilder.setContentText(
				appContext.getString(
					R.string.notification_download_queued_for_retry, attemptCount + 1
				)
			)
	}

}
