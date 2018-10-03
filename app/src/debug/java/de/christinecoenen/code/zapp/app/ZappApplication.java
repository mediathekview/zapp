package de.christinecoenen.code.zapp.app;

import android.app.Application;

import de.christinecoenen.code.zapp.utils.system.NotificationHelper;
import timber.log.Timber;


@SuppressWarnings("WeakerAccess")
public class ZappApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Timber.plant(new Timber.DebugTree());

		NotificationHelper.createBackgroundPlaybackChannel(this);
	}

}
