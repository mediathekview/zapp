package de.christinecoenen.code.zapp.app;

import android.app.Application;

import timber.log.Timber;


public class ZappApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Timber.plant(new Timber.DebugTree());
	}

}
