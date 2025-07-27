package de.christinecoenen.code.zapp.tv2.about

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv.settings.SettingsActivity
import de.christinecoenen.code.zapp.tv2.changelog.ChangelogActivity
import de.christinecoenen.code.zapp.tv2.faq.FaqActivity
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun AboutMenu() {
	AppTheme {
		Column(
			verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
		) {
			val context = LocalContext.current
			SettingsCard(
				titleRes = R.string.activity_settings_title,
				iconRes = R.drawable.ic_outline_settings_24,
				onClick = {
					context.startActivity(Intent(context, SettingsActivity::class.java))
				}
			)
			SettingsCard(
				titleRes = R.string.changelog_title,
				iconRes = R.drawable.ic_sharp_format_list_bulleted_24,
				onClick = {
					context.startActivity(ChangelogActivity.getStartIntent(context))
				}
			)

			SettingsCard(
				titleRes = R.string.faq_title,
				iconRes = R.drawable.ic_baseline_help_outline_24,
				onClick = {
					context.startActivity(FaqActivity.getStartIntent(context))
				}
			)
		}
	}
}
