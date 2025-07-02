package de.christinecoenen.code.zapp.tv2.live

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import de.christinecoenen.code.zapp.models.channels.ChannelModel

@Composable
fun ChannelList(
	channels: List<ChannelModel>,
	onChannelClick: (index: Int) -> Unit = {},
	onChannelFocus: (index: Int) -> Unit = {},
	onUnfocus: () -> Unit = {},
) {
	LazyRow(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		contentPadding = PaddingValues(48.dp),
		modifier = Modifier
			.onFocusChanged {
				if (!it.hasFocus) {
					onUnfocus()
				}
			}
	) {
		itemsIndexed(channels) { index, channel ->
			ChannelItem(
				name = channel.name,
				subtitle = channel.subtitle,
				logoResId = channel.drawableId,
				onClick = { onChannelClick(index) },
				onFocus = { onChannelFocus(index) },
			)
		}
	}
}
