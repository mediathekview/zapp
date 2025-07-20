package de.christinecoenen.code.zapp.tv2.mediathek

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
	val selectedShow by remember {
		derivedStateOf { if (selectedShowIndex == null) null else shows[selectedShowIndex!!] }
	}

	Row(
		modifier = Modifier
            .padding(horizontal = 58.dp)
            .padding(top = 104.dp)
	) {
		ShowList(
			showList = shows,
			selectedShowIndex = selectedShowIndex,
			onShowClick = { selectedShowIndex = it },
			modifier = Modifier.weight(0.5f)
		)

		selectedShow?.let {
			Spacer(Modifier.width(20.dp))

			ShowDetails(
				topic = it.topic,
				title = it.title,
				modifier = Modifier.weight(0.5f)
			)
		}
	}
}
