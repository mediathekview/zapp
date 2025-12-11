package de.christinecoenen.code.zapp.tv2.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.tv.material3.MaterialTheme
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.tv2.live.LiveScreen
import de.christinecoenen.code.zapp.tv2.player.PlayerScreen
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

	// TODO: move to components and maybe rename those NavKeys
	@Serializable
	private data object LiveScreen : NavKey

	@Serializable
	private data class Player(val videoInfo: VideoInfo) : NavKey

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			val backStack = rememberNavBackStack(LiveScreen)

			AppTheme {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(MaterialTheme.colorScheme.surface)
				) {
					NavDisplay(
						entryDecorators = listOf(
							rememberSaveableStateHolderNavEntryDecorator(),
							rememberViewModelStoreNavEntryDecorator()
						),
						backStack = backStack,
						modifier = Modifier.fillMaxSize(),
						entryProvider = entryProvider {
							entry<LiveScreen> {
								LiveScreen(
									onChannelClick = { channel ->
										backStack.add(Player(VideoInfo.fromChannel(channel)))
									}
								)
							}

							entry<Player> { key ->
								PlayerScreen(videoInfo = key.videoInfo)
							}

							// TODO: add remaining screens
						}
					)

					// TODO: add tab top navigation back in
					/*TopNavigation(
						selectedTabIndex = currentSelectedTabIndex,
						onTabSelected = { index -> navigationViewModel.selectMainTab(index) },
						tabStringIds = navigationViewModel.mainTabTitleResIds,
						modifier = Modifier.align(Alignment.TopCenter)
					)*/
				}
			}
		}
	}
}
