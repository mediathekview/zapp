package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.ColorHelper.themeColor
import de.christinecoenen.code.zapp.utils.system.NotificationHelper
import org.joda.time.DateTime

abstract class DownloadNotification(
	appContext: Context,
	title: String,
	persistedShowId: Int
) {
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
		.setPriority(NotificationManager.IMPORTANCE_MIN)
		.setSortKey(DateTime.now().millis.toString())
		.setCategory(Notification.CATEGORY_SERVICE)
		.setContentIntent(
			NavDeepLinkBuilder(appContext)
				.setGraph(R.navigation.nav_graph)
				.setDestination(R.id.mediathekDetailFragment)
				.setArguments(Bundle().apply {
					putSerializable("persisted_show_id", persistedShowId)
				})
				.createPendingIntent()
		)

	fun build() = notificationBuilder
		.build()
}
