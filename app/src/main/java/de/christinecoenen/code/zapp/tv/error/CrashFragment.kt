package de.christinecoenen.code.zapp.tv.error

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.leanback.app.ErrorSupportFragment
import org.acra.config.ConfigUtils
import org.acra.config.DialogConfiguration
import org.acra.dialog.CrashReportDialogHelper

class CrashFragment : ErrorSupportFragment() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val helper = CrashReportDialogHelper(requireContext(), requireActivity().intent)
		val dialogConfiguration =
			ConfigUtils.getPluginConfiguration(helper.config, DialogConfiguration::class.java)

		title = dialogConfiguration.title
		message = dialogConfiguration.text
		imageDrawable = ContextCompat.getDrawable(requireContext(), dialogConfiguration.resIcon)
		setDefaultBackground(false)

		helper.cancelReports()
	}

}
