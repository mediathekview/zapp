package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications

import android.app.NotificationManager
import android.content.Context
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.NotificationHelper

class DownloadCompletedEventNotification(
	appContext: Context,
	title: String,
	persistedShowId: Int
) : DownloadNotification(appContext, title, persistedShowId) {

	init {
		notificationBuilder
			.setChannelId(NotificationHelper.CHANNEL_ID_DOWNLOAD_EVENT)
			.setPriority(NotificationManager.IMPORTANCE_DEFAULT)
			.setSmallIcon(R.drawable.ic_outline_check_24)
			.setAutoCancel(true)
			.setContentText(appContext.getString(R.string.notification_download_complete))
	}

}
