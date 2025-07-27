package de.christinecoenen.code.zapp.tv2.about

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun SettingsCard(
	@StringRes titleRes: Int = R.string.menu_settings,
	@DrawableRes iconRes: Int = R.drawable.ic_sharp_format_list_bulleted_24,
	selected: Boolean = false,
	onClick: () -> Unit = {},
) {
	AppTheme {
		ListItem(
			selected = selected,
			onClick = onClick,
			headlineContent = {
				Text(stringResource(titleRes))
			},
			leadingContent = {
				Icon(
					painter = painterResource(iconRes),
					contentDescription = null,
					modifier = Modifier.size(ListItemDefaults.IconSize)
				)
			},
		)
	}
}
