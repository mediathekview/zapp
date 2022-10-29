package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications

import android.app.PendingIntent
import android.content.Context
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.NotificationHelper

class DownloadQueuedForRetryEventNotification(
	appContext: Context,
	title: String,
	persistedShowId: Int,
	attemptCount: Int,
	cancelIntent: PendingIntent
) : DownloadQueuedEventNotification(appContext, title, persistedShowId, cancelIntent) {

	init {
		notificationBuilder
			.setChannelId(NotificationHelper.CHANNEL_ID_DOWNLOAD_PROGRESS)
			.setContentText(
				appContext.getString(
					R.string.notification_download_queued_for_retry, attemptCount + 1
				)
			)
	}

}
