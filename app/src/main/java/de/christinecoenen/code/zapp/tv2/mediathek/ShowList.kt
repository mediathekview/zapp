package de.christinecoenen.code.zapp.tv2.mediathek

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.tv2.common.CircularProgress

@Composable
fun ShowList(
	showList: LazyPagingItems<MediathekShow>
) {
	LazyColumn {
		items(
			showList.itemCount,
			key = showList.itemKey { it.apiId }
		) { index ->
			val show = showList[index]!!

			ShowListItem(
				title = show.title,
				topic = show.topic,
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
