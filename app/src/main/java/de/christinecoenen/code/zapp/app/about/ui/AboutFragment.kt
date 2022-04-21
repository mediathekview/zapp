package de.christinecoenen.code.zapp.app.about.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.LibsConfiguration.LibsListener
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment
import com.mikepenz.aboutlibraries.util.SpecialButton
import de.christinecoenen.code.zapp.R.string
import de.christinecoenen.code.zapp.utils.system.IntentHelper

class AboutFragment : LibsSupportFragment() {

	private val buttonListener: LibsListener = object : AbstractLibsListener() {

		override fun onIconClicked(v: View) =
			IntentHelper.openUrl(requireContext(), getString(string.app_website_url))

		override fun onExtraClicked(v: View, specialButton: SpecialButton): Boolean =
			when (specialButton) {
				SpecialButton.SPECIAL1 -> {
					val action = AboutFragmentDirections.actionAboutFragmentToChangelogFragment()
					findNavController().navigate(action)
					true
				}
				SpecialButton.SPECIAL2 -> {
					val action = AboutFragmentDirections.toFaqFragment()
					findNavController().navigate(action)
					true
				}
				SpecialButton.SPECIAL3 -> {
					IntentHelper.sendMail(
						requireContext(),
						getString(string.support_mail),
						getString(string.about_feedback_mail_subject)
					)
					true
				}
				else -> false
			}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		val libsBuilder = LibsBuilder()
			.withAboutDescription(getString(string.aboutLibraries_description_text))
			.withListener(buttonListener)

		arguments?.putSerializable("data", libsBuilder)

		super.onCreate(savedInstanceState)
	}
}
