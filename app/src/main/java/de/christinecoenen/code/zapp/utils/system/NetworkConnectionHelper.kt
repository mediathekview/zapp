package de.christinecoenen.code.zapp.utils.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.core.net.ConnectivityManagerCompat
import java.lang.ref.WeakReference

class NetworkConnectionHelper(context: Context) {

	private val networkReceiver = NetworkReceiver()
	private val contextReference: WeakReference<Context> = WeakReference(context)
	private var networkChangedCallback: (() -> Unit)? = null

	fun startListenForNetworkChanges(networkChangedCallback: () -> Unit) {
		this.networkChangedCallback = networkChangedCallback

		val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
		contextReference.get()?.registerReceiver(networkReceiver, filter)
	}

	fun endListenForNetworkChanges() {
		networkChangedCallback = null
		contextReference.get()?.unregisterReceiver(networkReceiver)
	}

	val isConnectedToUnmeteredNetwork: Boolean
		get() {
			val context = contextReference.get() ?: return false

			val connectionManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
			return !ConnectivityManagerCompat.isActiveNetworkMetered(connectionManager)
		}


	private inner class NetworkReceiver : BroadcastReceiver() {

		override fun onReceive(context: Context, intent: Intent) {
			networkChangedCallback?.invoke()
		}

	}
}
