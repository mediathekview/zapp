package de.christinecoenen.code.zapp.utils.system;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.ref.WeakReference;
import java.util.Objects;


public class NetworkConnectionHelper {

	private final NetworkReceiver networkReceiver = new NetworkReceiver();
	private final WeakReference<Context> contextReference;
	private Listener listener;

	public NetworkConnectionHelper(Context context) {
		contextReference = new WeakReference<>(context);
	}

	public void startListenForNetworkChanges(Listener listener) {
		Context context = contextReference.get();
		if (context == null) {
			return;
		}

		this.listener = listener;
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(networkReceiver, filter);
	}

	public void endListenForNetworkChanges() {
		Context context = contextReference.get();
		if (context == null) {
			return;
		}

		this.listener = null;
		context.unregisterReceiver(networkReceiver);
	}

	public boolean isConnectedToWifiOrEthernet() {
		Context context = contextReference.get();
		if (context == null) {
			return false;
		}

		ConnectivityManager connectionManager = (ConnectivityManager)
			context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeInfo = Objects.requireNonNull(connectionManager).getActiveNetworkInfo();
		return activeInfo != null &&
			activeInfo.isConnected() &&
			(activeInfo.getType() == ConnectivityManager.TYPE_WIFI || activeInfo.getType() == ConnectivityManager.TYPE_ETHERNET );
	}


	private class NetworkReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (listener != null) {
				listener.onNetworkChanged();
			}
		}
	}

	public interface Listener {
		void onNetworkChanged();
	}
}
