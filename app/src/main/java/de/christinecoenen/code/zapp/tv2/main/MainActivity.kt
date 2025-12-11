package de.christinecoenen.code.zapp.tv2.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.tv.material3.MaterialTheme
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.tv2.about.AboutScreen
import de.christinecoenen.code.zapp.tv2.about.AboutScreenLocation
import de.christinecoenen.code.zapp.tv2.live.LiveScreen
import de.christinecoenen.code.zapp.tv2.live.LiveScreenLocation
import de.christinecoenen.code.zapp.tv2.main.navigation.MainScreenLocation
import de.christinecoenen.code.zapp.tv2.mediathek.MediaCenterLocation
import de.christinecoenen.code.zapp.tv2.mediathek.MediaCenterScreen
import de.christinecoenen.code.zapp.tv2.player.PlayerScreen
import de.christinecoenen.code.zapp.tv2.player.PlayerScreenLocation
import de.christinecoenen.code.zapp.tv2.theme.AppTheme

class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			val backStack = rememberNavBackStack(LiveScreenLocation)
			val mainLocations = listOf(LiveScreenLocation, MediaCenterLocation, AboutScreenLocation)
			var selectedMainLocation by remember { mutableStateOf(mainLocations.first()) }

			AppTheme {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(MaterialTheme.colorScheme.surface)
				) {
					NavDisplay(
						modifier = Modifier.fillMaxSize(),
						entryDecorators = listOf(
							rememberSaveableStateHolderNavEntryDecorator(),
							rememberViewModelStoreNavEntryDecorator()
						),
						backStack = backStack,
						entryProvider = entryProvider {
							entry<LiveScreenLocation> {
								LiveScreen(
									onChannelClick = { channel ->
										backStack.add(
											PlayerScreenLocation(VideoInfo.fromChannel(channel))
										)
									}
								)
							}

							entry<MediaCenterLocation> {
								MediaCenterScreen(
									onShowSelected = { show ->
										backStack.add(
											PlayerScreenLocation(VideoInfo.fromShow(show))
										)
									}
								)
							}

							entry<AboutScreenLocation> {
								AboutScreen()
							}

							entry<PlayerScreenLocation> { key ->
								PlayerScreen(videoInfo = key.videoInfo)
							}

							// TODO: add remaining screens
						}
					)

					// Show/hide main navigation based on backstack
					NavDisplay(
						modifier = Modifier.align(Alignment.TopCenter),
						backStack = backStack,
						entryProvider = { key ->
							when (key) {
								is MainScreenLocation -> NavEntry(key) {
									TopNavigation(
										locations = mainLocations,
										selectedLocation = selectedMainLocation,
										onLocationSelected = { location ->
											selectedMainLocation = location
											backStack.clear()
											backStack.add(location)
										},
										modifier = Modifier.align(Alignment.TopCenter)
									)
								}

								else -> NavEntry(key) {}
							}
						}
					)
				}
			}
		}
	}
}
