package de.christinecoenen.code.zapp.utils.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.christinecoenen.code.zapp.app.ZappApplication

class PackageUpdateReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent) {
		if (Intent.ACTION_MY_PACKAGE_REPLACED == intent.action) {
			val app = context.applicationContext as ZappApplication
			app.channelRepository.deleteCachedChannelInfos()
		}
	}
	
}
