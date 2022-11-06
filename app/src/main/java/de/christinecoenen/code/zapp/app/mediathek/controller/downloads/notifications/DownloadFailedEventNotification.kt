package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.ErrorType
import de.christinecoenen.code.zapp.utils.system.NotificationHelper

class DownloadFailedEventNotification(
	appContext: Context,
	title: String,
	persistedShowId: Int,
	errorType: ErrorType,
	retryIntent: PendingIntent
) : DownloadNotification(appContext, title, persistedShowId) {

	init {
		notificationBuilder
			.setChannelId(NotificationHelper.CHANNEL_ID_DOWNLOAD_EVENT)
			.setPriority(NotificationManager.IMPORTANCE_DEFAULT)
			.setSmallIcon(R.drawable.ic_outline_warning_amber_24)
			.setAutoCancel(true)
			.setSubText(errorType.toString())
			.setContentText(appContext.getString(R.string.notification_download_failed))
			.addAction(
				R.drawable.ic_refresh_white_24dp,
				appContext.getString(R.string.menu_retry),
				retryIntent
			)
	}

}
