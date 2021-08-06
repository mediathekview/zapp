package de.christinecoenen.code.zapp.app.about.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.mikepenz.aboutlibraries.Libs.SpecialButton
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.LibsConfiguration.LibsListener
import com.mikepenz.aboutlibraries.ui.LibsActivity
import com.mikepenz.aboutlibraries.util.LibsListenerImpl
import de.christinecoenen.code.zapp.R.string
import de.christinecoenen.code.zapp.utils.system.IntentHelper

class AboutActivity : LibsActivity() {

	companion object {
		fun getStartIntent(context: Context?): Intent = Intent(context, AboutActivity::class.java)
	}

	private val buttonListener: LibsListener = object : LibsListenerImpl() {

		override fun onIconClicked(v: View) =
			IntentHelper.openUrl(this@AboutActivity, getString(string.app_website_url))

		override fun onExtraClicked(v: View, specialButton: SpecialButton): Boolean =
			when (specialButton) {
				SpecialButton.SPECIAL1 -> {
					startActivity(ChangelogActivity.getStartIntent(this@AboutActivity))
					true
				}
				SpecialButton.SPECIAL2 -> {
					startActivity(FaqActivity.getStartIntent(this@AboutActivity))
					true
				}
				SpecialButton.SPECIAL3 -> {
					IntentHelper.sendMail(
						this@AboutActivity,
						getString(string.support_mail),
						getString(string.activity_about_feedback_mail_subject)
					)
					true
				}
				else -> super.onExtraClicked(v, specialButton)
			}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val intent = LibsBuilder()
			.withActivityTitle(getString(string.activity_about_title))
			.withAboutDescription(getString(string.aboutLibraries_description_text))
			.withFields(string::class.java.fields)
			.withAutoDetect(true)
			.withListener(buttonListener)
			.intent(this)

		setIntent(intent)
	}
}
