package de.christinecoenen.code.zapp.tv2.about

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
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
	// TODO: handle clicks

	AppTheme {
		Column(
			verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
		) {
			val context = LocalContext.current
			SettingsCard(
				titleRes = R.string.activity_settings_title,
				icon = Icons.Outlined.Settings,
				selected = false,
				onClick = {
					context.startActivity(Intent(context, SettingsActivity::class.java))
				}
			)
			SettingsCard(
				titleRes = R.string.changelog_title,
				icon = Icons.AutoMirrored.Outlined.List,
				selected = false,
				onClick = {
					context.startActivity(ChangelogActivity.getStartIntent(context))
				}
			)
			// TODO: where is our help icon?
			SettingsCard(
				titleRes = R.string.faq_title,
				icon = Icons.Outlined.Info,
				selected = false,
				onClick = {
					context.startActivity(FaqActivity.getStartIntent(context))
				}
			)
		}
	}
}
