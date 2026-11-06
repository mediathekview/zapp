package de.christinecoenen.code.zapp.app.personal.series

import android.app.Notification
import android.content.Context
import android.os.Bundle
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.collections.ShowCollection
import de.christinecoenen.code.zapp.utils.system.ColorHelper.themeColor
import de.christinecoenen.code.zapp.utils.system.NotificationHelper

/**
 * Notification telling the user that a collection contains new shows.
 */
class CollectionUpdateNotification(
	context: Context,
	collection: ShowCollection,
	newShowCount: Int,
) {

	private val notification: Notification = NotificationCompat.Builder(
		context,
		NotificationHelper.CHANNEL_ID_COLLECTION_UPDATE
	)
		.setContentTitle(
			context.getString(R.string.notification_collection_update_title, collection.name)
		)
		.setContentText(
			context.resources.getQuantityString(
				R.plurals.notification_collection_update_text,
				newShowCount,
				newShowCount
			)
		)
		.setSmallIcon(R.drawable.ic_outline_video_library_24)
		.setColor(
			ContextThemeWrapper(context, R.style.AppTheme)
				.themeColor(android.R.attr.colorPrimary)
		)
		.setAutoCancel(true)
		.setOnlyAlertOnce(true)
		.setContentIntent(
			NavDeepLinkBuilder(context)
				.setGraph(R.navigation.nav_graph)
				.setDestination(R.id.seriesDetailFragment)
				.setArguments(Bundle().apply {
					putInt("collection_id", collection.id)
				})
				.createPendingIntent()
		)
		.build()

	fun build(): Notification = notification
}
