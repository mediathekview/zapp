package de.christinecoenen.code.zapp.tv2.live

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp
import de.christinecoenen.code.zapp.models.channels.ChannelModel

@Composable
fun ChannelList(
	channels: List<ChannelModel>,
	selectedChannel: ChannelModel = channels.first(),
	onChannelSelected: (channel: ChannelModel) -> Unit = {},
	onChannelClick: (channel: ChannelModel) -> Unit = {},
) {
	val focusRequester = remember { FocusRequester() }

	LazyRow(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		contentPadding = PaddingValues(48.dp),
		modifier = Modifier
			.focusGroup()
			.focusRestorer(focusRequester)
	) {
		items(channels) { channel ->
			val isSelected = channel.id == selectedChannel.id

			ChannelItem(
				name = channel.name,
				subtitle = channel.subtitle,
				logoResId = channel.drawableId,
				isSelected = isSelected,
				onClick = { onChannelClick(channel) },
				onFocus = { onChannelSelected(channel) },
				modifier = Modifier
					.then(if (isSelected) Modifier.focusRequester(focusRequester) else Modifier)
			)
		}
	}
}
