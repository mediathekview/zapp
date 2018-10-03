package de.christinecoenen.code.zapp.utils.system;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import de.christinecoenen.code.zapp.R;
import timber.log.Timber;

public class NotificationHelper {

	public static final int BACKGROUND_PLAYBACK_NOTIFICATION_ID = 23;
	public static final String BACKGROUND_PLAYBACK_CHANNEL_ID = "background_playback";

	public static void createBackgroundPlaybackChannel(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			// Create the NotificationChannel, but only on API 26+ because
			// the NotificationChannel class is new and not in the support library
			return;
		}

		CharSequence name = context.getString(R.string.notification_channel_name_background_playback);
		int importance = NotificationManager.IMPORTANCE_LOW;
		NotificationChannel channel = new NotificationChannel(BACKGROUND_PLAYBACK_CHANNEL_ID, name, importance);

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
