package de.christinecoenen.code.zapp.tv2.mediathek

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv2.main.navigation.Location
import de.christinecoenen.code.zapp.tv2.theme.TvScreenPreview
import org.koin.androidx.compose.koinViewModel

class MediaCenterScreenLocation : Location(
	titleResId = R.string.activity_main_tab_mediathek,
	isMainTab = true,
)

@TvScreenPreview
@Composable
fun MediaCenterScreen(
	viewModel: MediathekScreenViewModel = koinViewModel()
) {
	val showFlow = remember { viewModel.shows }
	val shows = showFlow.collectAsLazyPagingItems()
	var selectedShowIndex by rememberSaveable { mutableStateOf<Int?>(null) }
	/*val selectedShow by remember {
		derivedStateOf { if (selectedShowIndex == null) null else shows[selectedShowIndex!!] }
	}*/
	var searchText by rememberSaveable { mutableStateOf("") }

	LaunchedEffect(searchText) {
		viewModel.loadShows(searchText)
	}

	Column(
		modifier = Modifier
			.padding(horizontal = 202.dp)
			.padding(top = 104.dp)
	) {
		// TODO: style
		TextField(
			value = searchText,
			onValueChange = { searchText = it },
			singleLine = true,
			modifier = Modifier.fillMaxWidth()
		)
		Spacer(Modifier.height(20.dp))
		ShowList(
			showList = shows,
			selectedShowIndex = selectedShowIndex,
			onShowClick = { selectedShowIndex = it },
			modifier = Modifier.weight(0.5f)
		)
	}
}
