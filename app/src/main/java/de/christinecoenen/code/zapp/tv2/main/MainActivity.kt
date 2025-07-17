package de.christinecoenen.code.zapp.tv2.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.tv2.about.AboutScreen
import de.christinecoenen.code.zapp.tv2.about.AboutScreenLocation
import de.christinecoenen.code.zapp.tv2.mediathek.MediaCenterScreen
import de.christinecoenen.code.zapp.tv2.mediathek.MediaCenterScreenLocation
import de.christinecoenen.code.zapp.tv2.live.LiveScreen
import de.christinecoenen.code.zapp.tv2.live.LiveScreenLocation
import de.christinecoenen.code.zapp.tv2.main.navigation.Location
import de.christinecoenen.code.zapp.tv2.main.navigation.NavigationViewModel
import de.christinecoenen.code.zapp.tv2.player.PlayerLocation
import de.christinecoenen.code.zapp.tv2.player.PlayerScreen
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val navigationViewModel: NavigationViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    )
                    {
                        val currentLocation by navigationViewModel.currentLocation
                            .collectAsStateWithLifecycle(Location())
                        val currentSelectedTabIndex by navigationViewModel.currentSelectedTabIndex
                            .collectAsStateWithLifecycle(-1)

                        BackHandler(!currentLocation.isMainTab) {
                            navigationViewModel.closeCurrentScreen()
                        }

                        when (val location = currentLocation) {
                            is LiveScreenLocation -> LiveScreen(
                                onChannelClick = { channel ->
                                    navigationViewModel.showScreen(
                                        PlayerLocation(videoInfo = VideoInfo.fromChannel(channel))
                                    )
                                }
                            )

                            is MediaCenterScreenLocation -> MediaCenterScreen()

                            is AboutScreenLocation -> AboutScreen()

                            is PlayerLocation -> PlayerScreen(location.videoInfo)
                        }

                        if (currentLocation.isMainTab) {
                            TopNavigation(
                                selectedTabIndex = currentSelectedTabIndex,
                                onTabSelected = { index -> navigationViewModel.selectMainTab(index) },
                                tabStringIds = navigationViewModel.mainTabTitleResIds,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        }
    }

}
