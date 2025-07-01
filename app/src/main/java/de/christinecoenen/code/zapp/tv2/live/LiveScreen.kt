package de.christinecoenen.code.zapp.tv2.live

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.channels.json.JsonChannelList
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun LiveScreen() {

	val context = LocalContext.current
	val channels = rememberSaveable { JsonChannelList(context).list }
	var focusedChannel by remember { mutableStateOf<ChannelModel?>(null) }

	Text(
		text = focusedChannel?.name.orEmpty(),
		style = MaterialTheme.typography.headlineLarge,
		color = MaterialTheme.colorScheme.onSurface,
	)

	ChannelList(
		channels = channels,
		onChannelClick = {},
		onChannelFocus = { index -> focusedChannel = channels.get(index) }
	)
}


