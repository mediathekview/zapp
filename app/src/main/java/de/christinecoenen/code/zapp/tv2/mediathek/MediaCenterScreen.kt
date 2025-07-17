package de.christinecoenen.code.zapp.tv2.mediathek

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv2.main.navigation.Location
import de.christinecoenen.code.zapp.tv2.theme.TvScreenPreview
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class MediaCenterScreenLocation : Location(
	titleResId = R.string.activity_main_tab_mediathek,
	isMainTab = true,
)

@TvScreenPreview
@Composable
fun MediaCenterScreen(
	viewModel: MediathekScreenViewModel = koinViewModel {
		parametersOf(MutableStateFlow(""))
	}
) {
	val shows = viewModel.shows.collectAsLazyPagingItems()

	Row(
		modifier = Modifier
			.padding(horizontal = 58.dp)
			.padding(top = 104.dp)
	) {
		ShowList(shows)
	}
}
