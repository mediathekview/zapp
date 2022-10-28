package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.revisited

import android.app.Notification
import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.ColorHelper.themeColor
import de.christinecoenen.code.zapp.utils.system.NotificationHelper
import org.joda.time.DateTime

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
		.setColor(
			ContextThemeWrapper(appContext, R.style.AppTheme)
				.themeColor(android.R.attr.colorPrimary)
		)
		.setSortKey(DateTime.now().millis.toString())
		.setCategory(Notification.CATEGORY_SERVICE)

	fun build() = notificationBuilder
		.build()
}
