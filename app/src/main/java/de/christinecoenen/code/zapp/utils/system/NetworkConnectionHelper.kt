package de.christinecoenen.code.zapp.utils.system

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import androidx.core.net.ConnectivityManagerCompat

/**
 * Detects changes in network status (metered or unmetered).
 * Call startListenForNetworkChanges to get callbacks whenever isConnectedToUnmeteredNetwork
 * changes.
 * Listeners will be called on main thread.
 */
class NetworkConnectionHelper(context: Context) {

	var isConnectedToUnmeteredNetwork: Boolean = true
		private set

	private var wasConnectedToUnmeteredNetwork = false

	private val mainThreadHandler = Handler(Looper.getMainLooper())
	private val connectivityManager =
		context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
	private var networkChangedCallback: (() -> Unit)? = null

	private val networkCallback = object : ConnectivityManager.NetworkCallback() {
		override fun onAvailable(network: Network) {
			onNetworkChanged()
		}

		override fun onLost(network: Network) {
			onNetworkChanged()
		}

		override fun onUnavailable() {
			onNetworkChanged()
		}

		override fun onCapabilitiesChanged(
			network: Network,
			networkCapabilities: NetworkCapabilities
		) {
			onNetworkChanged()
		}
	}

	fun startListenForNetworkChanges(networkChangedCallback: () -> Unit) {
		this.networkChangedCallback = networkChangedCallback

		connectivityManager.registerNetworkCallback(
			NetworkRequest.Builder().build(),
			networkCallback
		)
	}

	fun endListenForNetworkChanges() {
		networkChangedCallback = null
		connectivityManager.unregisterNetworkCallback(networkCallback)
	}

	private fun onNetworkChanged() {
		this.isConnectedToUnmeteredNetwork =
			!ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager)

		if (isConnectedToUnmeteredNetwork != wasConnectedToUnmeteredNetwork) {
			wasConnectedToUnmeteredNetwork = this.isConnectedToUnmeteredNetwork

			mainThreadHandler.post {
				networkChangedCallback?.invoke()
			}
		}
	}
}
