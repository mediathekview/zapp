package de.christinecoenen.code.zapp.app.player;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import java.util.Objects;

class PlayerWakeLocks {

	private PowerManager.WakeLock wakeLock;
	private WifiManager.WifiLock wifiLock;

	PlayerWakeLocks(Context context, String tag) {
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = Objects.requireNonNull(powerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);

		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		wifiLock = Objects.requireNonNull(wifiManager).createWifiLock(WifiManager.WIFI_MODE_FULL, tag);
	}

	void acquire(long millis) {
		wakeLock.acquire(millis);
		wifiLock.acquire();
	}

	void destroy() {
		release();
		wakeLock = null;
		wifiLock = null;
	}

	private void release() {
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
		if (wifiLock.isHeld()) {
			wifiLock.release();
		}
	}
}
