package de.christinecoenen.code.zapp.app

import android.content.Context
import org.acra.ACRA
import timber.log.Timber
import timber.log.Timber.DebugTree

class ZappApplication : ZappApplicationBase() {

	override fun setUpLogging() {
		Timber.plant(DebugTree())
	}

	override fun attachBaseContext(base: Context) {
		super.attachBaseContext(base)

		// The following line triggers the initialization of ACRA
		ACRA.init(this)
	}
	
}
