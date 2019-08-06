package de.christinecoenen.code.zapp.utils.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.christinecoenen.code.zapp.app.ZappApplication;

public class PackageUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
			ZappApplication app = (ZappApplication) context.getApplicationContext();
			app.getChannelRepository().deleteCachedChannelInfos();
		}
	}
}
