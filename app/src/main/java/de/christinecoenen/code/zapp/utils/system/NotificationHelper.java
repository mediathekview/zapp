package de.christinecoenen.code.zapp.utils.system;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import de.christinecoenen.code.zapp.R;
import timber.log.Timber;

public class NotificationHelper {

	public static final int BACKGROUND_PLAYBACK_NOTIFICATION_ID = 23;

	public static final String CHANNEL_ID_BACKGROUND_PLAYBACK = "background_playback";
	public static final String CHANNEL_ID_DOWNLOAD_PROGRESS = "download_progress";
	public static final String CHANNEL_ID_DOWNLOAD_EVENT = "download_event";

	public static void createBackgroundPlaybackChannel(Context context) {
		createNotificationChannel(context, CHANNEL_ID_BACKGROUND_PLAYBACK,
			R.string.notification_channel_name_background_playback, true);
	}

	public static void createDownloadProgressChannel(Context context) {
		createNotificationChannel(context, CHANNEL_ID_DOWNLOAD_PROGRESS,
			R.string.notification_channel_name_download_progress, true);
	}

	public static void createDownloadEventChannel(Context context) {
		createNotificationChannel(context, CHANNEL_ID_DOWNLOAD_EVENT,
			R.string.notification_channel_name_download_event, false);
	}

	private static void createNotificationChannel(Context context, String channelId, int nameResId, boolean isLowImportance) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			// Create the NotificationChannel, but only on API 26+ because
			// the NotificationChannel class is new and not in the support library
			return;
		}

		CharSequence name = context.getString(nameResId);
		int importance = isLowImportance ? NotificationManager.IMPORTANCE_LOW : NotificationManager.IMPORTANCE_DEFAULT;
		NotificationChannel channel = new NotificationChannel(channelId, name, importance);

		// Register the channel with the system; you can't change the importance
		// or other notification behaviors after this
		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		if (notificationManager == null) {
			Timber.w("NotificationManager not found");
		} else {
			notificationManager.createNotificationChannel(channel);
		}
	}
}
