package de.christinecoenen.code.zapp.tv.error

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.leanback.app.ErrorSupportFragment
import de.christinecoenen.code.zapp.R
import org.acra.config.DialogConfiguration
import org.acra.config.getPluginConfiguration
import org.acra.dialog.CrashReportDialogHelper

class CrashFragment : ErrorSupportFragment() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val helper = CrashReportDialogHelper(requireContext(), requireActivity().intent)

		val dialogConfiguration =
			helper.config.getPluginConfiguration(DialogConfiguration::class.java)
		val iconResId = dialogConfiguration.resIcon ?: R.drawable.ic_zapp_tv_small

		title = dialogConfiguration.title
		message = dialogConfiguration.text
		imageDrawable = ContextCompat.getDrawable(requireContext(), iconResId)

		setDefaultBackground(false)

		helper.cancelReports()
	}

}
