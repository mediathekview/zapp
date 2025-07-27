package de.christinecoenen.code.zapp.tv2.mediathek

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.tv2.common.CircularProgress

@Composable
fun ShowList(
	modifier: Modifier = Modifier,
	showList: LazyPagingItems<MediathekShow>,
	onShowClick: (index: Int) -> Unit = {},
) {
	val listState: LazyListState = rememberLazyListState()

	LazyColumn(
		state = listState,
		verticalArrangement = Arrangement.spacedBy(8.dp),
		modifier = modifier
	) {
		items(
			showList.itemCount,
			key = showList.itemKey { it.apiId }
		) { index ->
			val show = showList[index]!!

			ShowListItem(
				title = show.title,
				topic = show.topic,
				chanel = show.channel,
				duration = show.formattedDuration,
				releasedAt = show.formattedTimestamp.toString(),
				selected = false,
				onClick = { onShowClick(index) }
			)
		}

		if (!showList.loadState.isIdle) {
			item {
				CircularProgress()
			}
		}

		// TODO: show error state
	}
}
