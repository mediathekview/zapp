package de.christinecoenen.code.zapp.tv2.live

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.channels.json.JsonChannelList
import de.christinecoenen.code.zapp.tv2.theme.TvPreview
import org.koin.androidx.compose.koinViewModel

@TvPreview
@Composable
fun LiveScreen(programInfoViewModel: ProgramInfoViewModel = koinViewModel()) {

	val context = LocalContext.current
	val channels = rememberSaveable { JsonChannelList(context).list }
	var focusedChannel by remember { mutableStateOf<ChannelModel?>(null) }

	LaunchedEffect(focusedChannel?.id) {
		focusedChannel?.let { programInfoViewModel.setChannelId(it.id) }
	}

	val title by programInfoViewModel.titleFlow.collectAsStateWithLifecycle("")
	val subtitle by programInfoViewModel.subtitleFlow.collectAsStateWithLifecycle(null)
	val description by programInfoViewModel.descriptionFlow.collectAsStateWithLifecycle(null)
	val time by programInfoViewModel.timeFlow.collectAsStateWithLifecycle(null)

	Column(
		verticalArrangement = Arrangement.Bottom,
		modifier = Modifier.fillMaxSize()
	) {
		if (focusedChannel != null) {
			ChannelInfo(
				showTitle = title,
				showSubtitle = subtitle,
				description = description,
				time = time,
				modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(horizontal = 58.dp)
			)
		}

		ChannelList(
			channels = channels,
			onChannelClick = {},
			onChannelFocus = { index -> focusedChannel = channels[index] },
			onUnfocus = { focusedChannel = null }
		)
	}
}


