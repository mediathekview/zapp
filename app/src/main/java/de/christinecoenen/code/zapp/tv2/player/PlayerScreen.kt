package de.christinecoenen.code.zapp.tv2.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.ui.compose.PlayerSurface
import de.christinecoenen.code.zapp.app.player.Player
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.tv2.common.CircularProgress
import de.christinecoenen.code.zapp.tv2.main.navigation.Location
import org.koin.compose.koinInject

data class PlayerLocation(
    val videoInfo: VideoInfo,
) : Location()

@Composable
fun PlayerScreen(
    videoInfo: VideoInfo,
    player: Player = koinInject<Player>(),
) {
    var controllerVisible by remember { mutableStateOf(false) }
    val error by player.errorResourceId.collectAsState(null)

    BackHandler(controllerVisible) {
        controllerVisible = false
    }

    LaunchedEffect(Unit) {
        player.load(videoInfo)
        player.resume()
    }

    DisposableEffect(Unit) {
        onDispose {
            player.destroy()
        }
    }

    PlayerSurface(
        player = player.exoPlayer,
        modifier = Modifier.clickable {
            controllerVisible = !controllerVisible
        }
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // TODO: hide loading indicator when not buffering
        // CircularProgress()

        PlayerError(error)
    }

    // TODO: hide with delay when video is playing
    ControllerOverlay(
        title = videoInfo.title,
        subtitle = videoInfo.subtitle,
        isVisible = controllerVisible,
    )
}
