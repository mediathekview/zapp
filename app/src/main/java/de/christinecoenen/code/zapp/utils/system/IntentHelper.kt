package de.christinecoenen.code.zapp.utils.system

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import de.christinecoenen.code.zapp.R

object IntentHelper {

	/**
	 * Open the given url in a new (external) activity. If no app is found
	 * that can handle this intent, a system chooser is shown.
	 *
	 * @param context
	 * @param url
	 */
	@JvmStatic
	fun openUrl(context: Context, url: String?) {
		val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

		try {
			context.startActivity(browserIntent)
		} catch (e: ActivityNotFoundException) {
			context.startActivity(Intent.createChooser(browserIntent, context.getString(R.string.action_open)))
		}
	}

	/**
	 * Opens the default mail app. If no app is found that can handle
	 * this intent, a a system chooser is shown.
	 *
	 * @param context
	 * @param mail    receiver mail address
	 * @param subject mail subject line
	 */
	fun sendMail(context: Context, mail: String, subject: String?) {
		val feedbackIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$mail"))
		feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, subject)

		try {
			context.startActivity(feedbackIntent)
		} catch (e: ActivityNotFoundException) {
			context.startActivity(Intent.createChooser(feedbackIntent, context.getString(R.string.action_send_mail)))
		}
	}
}
