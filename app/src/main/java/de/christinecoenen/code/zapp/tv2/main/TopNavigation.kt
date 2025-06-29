package de.christinecoenen.code.zapp.tv2.main

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun TopNavigation(
	modifier: Modifier = Modifier
) {
	var selectedTabIndex by remember { mutableIntStateOf(0) }
	val tabs = listOf(
		stringResource(R.string.activity_main_tab_live),
		stringResource(R.string.activity_main_tab_mediathek),
		stringResource(R.string.menu_about_short),
	)

	AppTheme {
		TabRow(
			selectedTabIndex = selectedTabIndex,
			modifier = modifier.padding(top = 32.dp, bottom = 16.dp)
		) {
			tabs.forEachIndexed { index, tab ->
				Tab(
					selected = selectedTabIndex == index,
					onFocus = { selectedTabIndex = index },
				) {
					Text(
						text = tab,
						modifier = Modifier
							.padding(
								horizontal = 16.dp,
								vertical = 10.dp
							)
					)
				}
			}
		}
	}
}
