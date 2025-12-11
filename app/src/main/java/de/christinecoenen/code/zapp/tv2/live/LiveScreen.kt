package de.christinecoenen.code.zapp.tv2.live

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.tv2.main.navigation.MainScreenLocation
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object LiveScreenLocation : MainScreenLocation(
	titleResId = R.string.activity_main_tab_live,
)

@Composable
fun LiveScreen(
	liveScreenViewModel: LiveScreenViewModel = koinViewModel(),
	programInfoViewModel: ProgramInfoViewModel = koinViewModel(),
	onChannelClick: (channel: ChannelModel) -> Unit = {},
) {
	val selectedChannel by liveScreenViewModel.selectedChannel.collectAsState()

	LaunchedEffect(selectedChannel.id) {
		selectedChannel.let { programInfoViewModel.setChannelId(it.id) }
	}

	val title by programInfoViewModel.titleFlow.collectAsStateWithLifecycle("")
	val subtitle by programInfoViewModel.subtitleFlow.collectAsStateWithLifecycle(null)
	val description by programInfoViewModel.descriptionFlow.collectAsStateWithLifecycle(null)
	val time by programInfoViewModel.timeFlow.collectAsStateWithLifecycle(null)

	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		StreamPreviewImage(
			streamUrl = selectedChannel.streamUrl,
			modifier = Modifier
				.width(758.dp)
				.align(Alignment.TopEnd)
		)
	}

	Column(
		verticalArrangement = Arrangement.Bottom,
		modifier = Modifier.fillMaxSize()
	) {
		ChannelInfo(
			showTitle = title,
			showSubtitle = subtitle,
			description = description,
			time = time,
			modifier = Modifier
				.fillMaxWidth(0.7f)
				.padding(horizontal = 58.dp)
		)

		ChannelList(
			channels = liveScreenViewModel.channels,
			selectedChannel = selectedChannel,
			onChannelClick = { channel -> onChannelClick(channel) },
			onChannelSelected = { channel -> liveScreenViewModel.setSelectedChannel(channel) },
		)
	}
}


