package de.christinecoenen.code.zapp.tv2.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.tv2.about.AboutScreenLocation
import de.christinecoenen.code.zapp.tv2.live.LiveScreenLocation
import de.christinecoenen.code.zapp.tv2.main.navigation.MainScreenLocation
import de.christinecoenen.code.zapp.tv2.mediathek.MediaCenterLocation
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun TopNavigation(
	modifier: Modifier = Modifier,
	locations: List<MainScreenLocation> = listOf(
		LiveScreenLocation,
		MediaCenterLocation,
		AboutScreenLocation
	),
	selectedLocation: MainScreenLocation = locations.first(),
	onLocationSelected: (location: MainScreenLocation) -> Unit = {}
) {
	AppTheme {
		val focusRequester = remember { FocusRequester() }
		val selectedTabIndex = locations.indexOf(selectedLocation)
		var hasFocus by remember { mutableStateOf(false) }

		BackHandler(selectedTabIndex != 0 || !hasFocus) {
			onLocationSelected(locations.first())
			focusRequester.requestFocus()
		}

		// TODO: this seems to grap focus when navigating back from non main screen
		LaunchedEffect(Unit) {
			focusRequester.requestFocus()
		}

		TabRow(
			selectedTabIndex = selectedTabIndex,
			modifier = modifier
				.padding(top = 32.dp, bottom = 16.dp)
				.focusGroup()
				.focusRestorer()
				.onFocusChanged { hasFocus = it.hasFocus }
		) {
			locations.forEach { location ->
				val isSelected = location == selectedLocation

				Tab(
					selected = isSelected,
					onFocus = { onLocationSelected(location) },
					modifier = Modifier
						.then(if (isSelected) Modifier.focusRequester(focusRequester) else Modifier)
				) {
					Text(
						text = stringResource(location.titleResId),
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
