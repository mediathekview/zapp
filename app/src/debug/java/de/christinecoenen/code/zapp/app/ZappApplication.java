package de.christinecoenen.code.zapp.app;

import timber.log.Timber;


@SuppressWarnings("WeakerAccess")
public class ZappApplication extends ZappApplicationBase {

	@Override
	protected void setUpLogging() {
		Timber.plant(new Timber.DebugTree());
	}

}
