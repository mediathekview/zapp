package de.christinecoenen.code.zapp.app.mediathek.ui.helper

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.NoNetworkException
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException
import timber.log.Timber

object DownloadExceptionToastExtensions {

	fun Fragment.showDownloadExeptionToast(throwable: Throwable) {
		when (throwable) {
			is WrongNetworkConditionException -> {
				Snackbar
					.make(
						requireView(),
						R.string.error_mediathek_download_over_unmetered_network_only,
						Snackbar.LENGTH_LONG
					)
					.setAction(R.string.activity_settings_title) {
						findNavController().navigate(R.id.global_to_settingsFragment)
					}
					.show()
			}

			is NoNetworkException -> {
				Snackbar
					.make(
						requireView(),
						R.string.error_mediathek_download_no_network,
						Snackbar.LENGTH_LONG
					)
					.show()
			}

			else -> {
				Snackbar
					.make(
						requireView(),
						R.string.error_mediathek_generic_start_download_error,
						Snackbar.LENGTH_LONG
					)
					.show()
				Timber.e(throwable)
			}
		}
	}
}
