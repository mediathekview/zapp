package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.NotificationHelper

open class DownloadQueuedEventNotification(
	appContext: Context,
	title: String,
	persistedShowId: Int,
	cancelIntent: PendingIntent
) : DownloadNotification(appContext, title, persistedShowId) {

	init {
		notificationBuilder
			.setChannelId(NotificationHelper.CHANNEL_ID_DOWNLOAD_PROGRESS)
			.setContentText(appContext.getString(R.string.notification_download_queued))
			.setPriority(NotificationManager.IMPORTANCE_MIN)
			.setOngoing(true)
			.setSilent(true)
			.setProgress(0, 0, true)
			.setSmallIcon(android.R.drawable.stat_sys_download)
			.addAction(
				R.drawable.ic_baseline_close_24,
				appContext.getString(R.string.action_cancel),
				cancelIntent
			)
	}

}
