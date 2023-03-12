package de.christinecoenen.code.zapp.utils.system

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import de.christinecoenen.code.zapp.R
import timber.log.Timber

object NotificationHelper {

	const val BACKGROUND_PLAYBACK_NOTIFICATION_ID = 23

	const val CHANNEL_ID_BACKGROUND_PLAYBACK = "background_playback"
	const val CHANNEL_ID_DOWNLOAD_PROGRESS = "download_progress"
	const val CHANNEL_ID_DOWNLOAD_EVENT = "download_event"

	fun hasNotificationPermissionGranted(context: Context): Boolean {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
			return true
		}

		return ActivityCompat.checkSelfPermission(
			context,
			Manifest.permission.POST_NOTIFICATIONS
		) == PackageManager.PERMISSION_GRANTED;
	}

	@JvmStatic
	fun createBackgroundPlaybackChannel(context: Context) {
		createNotificationChannel(
			context, CHANNEL_ID_BACKGROUND_PLAYBACK,
			R.string.notification_channel_name_background_playback, true
		)
	}

	fun createDownloadProgressChannel(context: Context) {
		createNotificationChannel(
			context, CHANNEL_ID_DOWNLOAD_PROGRESS,
			R.string.notification_channel_name_download_progress, true
		)
	}

	fun createDownloadEventChannel(context: Context) {
		createNotificationChannel(
			context, CHANNEL_ID_DOWNLOAD_EVENT,
			R.string.notification_channel_name_download_event, false
		)
	}

	private fun createNotificationChannel(
		context: Context,
		channelId: String,
		nameResId: Int,
		isLowImportance: Boolean
	) {
		val name: CharSequence = context.getString(nameResId)
		val importance =
			if (isLowImportance) NotificationManager.IMPORTANCE_LOW
			else NotificationManager.IMPORTANCE_DEFAULT

		val channel = NotificationChannel(channelId, name, importance)

		// Register the channel with the system; you can't change the importance
		// or other notification behaviors after this
		val notificationManager = context.getSystemService(NotificationManager::class.java)

		if (notificationManager == null) {
			Timber.w("NotificationManager not found")
		} else {
			notificationManager.createNotificationChannel(channel)
		}
	}
}
