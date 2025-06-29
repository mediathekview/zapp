package de.christinecoenen.code.zapp.tv2.about

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.models.channels.IChannelList
import de.christinecoenen.code.zapp.models.channels.json.JsonChannelList
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun LiveScreen(channels: IChannelList = JsonChannelList(LocalContext.current)) {
	var selectedIndex by remember { mutableIntStateOf(0) }

	LazyVerticalGrid(
		columns = GridCells.Adaptive(minSize = 250.dp)
	) {
		itemsIndexed(channels.list) { index, channel ->
			ListItem(
				selected = index == selectedIndex,
				onClick = { selectedIndex = index },
				headlineContent = {
					Text(
						text = channel.name,
						style = MaterialTheme.typography.headlineLarge
					)
				}
			)
		}
	}
}
