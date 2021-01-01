package de.christinecoenen.code.zapp.app

import timber.log.Timber
import timber.log.Timber.DebugTree

class ZappApplication : ZappApplicationBase() {

	override fun setUpLogging() {
		Timber.plant(DebugTree())
	}
	
}
