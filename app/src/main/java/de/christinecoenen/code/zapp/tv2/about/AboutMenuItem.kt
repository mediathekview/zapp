package de.christinecoenen.code.zapp.tv2.about

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
	icon: ImageVector = Icons.Outlined.Settings,
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
					imageVector = icon,
					contentDescription = null,
					modifier = Modifier.size(ListItemDefaults.IconSize)
				)
			},
		)
	}
}
