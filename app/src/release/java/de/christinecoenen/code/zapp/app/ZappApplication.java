package de.christinecoenen.code.zapp.app;

import android.content.Context;

import org.acra.ACRA;

import timber.log.Timber;

public class ZappApplication extends ZappApplicationBase {

	@Override
	protected void setUpLogging() {
		Timber.plant(new Timber.DebugTree());
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);

		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}

}
