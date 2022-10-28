package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.NotificationHelper

abstract class DownloadEventNotification(
	appContext: Context,
	title: String
) {

	// TODO: make notification clickable
	protected val notificationBuilder = NotificationCompat.Builder(
		appContext,
		NotificationHelper.CHANNEL_ID_DOWNLOAD_EVENT
	)
		.setContentTitle(title)
		.setTicker(title)
		.setOnlyAlertOnce(true)
		.setPriority(NotificationManager.IMPORTANCE_MIN)
		.setCategory(Notification.CATEGORY_SERVICE)
		.setCategory(Notification.CATEGORY_SERVICE)

	fun build() = notificationBuilder
		.build()
}
