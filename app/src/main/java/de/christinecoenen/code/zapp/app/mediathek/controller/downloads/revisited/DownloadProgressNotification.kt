package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.ColorHelper.themeColor
import de.christinecoenen.code.zapp.utils.system.NotificationHelper
import org.joda.time.DateTime

class DownloadProgressNotification(
	appContext: Context,
	title: String,
	cancelIntent: PendingIntent
) {

	// TODO: make notification clickable
	private val progressNotificationBuilder = NotificationCompat.Builder(
		appContext,
		NotificationHelper.CHANNEL_ID_DOWNLOAD_PROGRESS
	)
		.setContentTitle(title)
		.setTicker(title)
		.setContentText(appContext.getString(R.string.notification_download_downloading))
		.setColor(
			ContextThemeWrapper(appContext, R.style.AppTheme)
				.themeColor(android.R.attr.colorPrimary)
		)
		.setOngoing(true)
		.setSmallIcon(android.R.drawable.stat_sys_download)
		.setPriority(NotificationManager.IMPORTANCE_MIN)
		.setCategory(Notification.CATEGORY_SERVICE)
		.setSortKey(DateTime.now().millis.toString())
		.addAction(
			R.drawable.ic_baseline_close_24,
			appContext.getString(R.string.action_cancel),
			cancelIntent
		)

	fun build(progress: Int) = progressNotificationBuilder
		.setProgress(100, progress, progress == 0)
		.build()
}
