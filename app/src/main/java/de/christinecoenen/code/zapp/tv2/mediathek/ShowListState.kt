package de.christinecoenen.code.zapp.tv2.mediathek

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv2.common.CircularProgress
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun ShowListState(
	modifier: Modifier = Modifier,
	loadState: LoadState = LoadState.Loading,
	onRetryClick: () -> Unit = {},
) {
	if (loadState is LoadState.NotLoading) {
		return
	}

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier.padding(16.dp)
	) {
		when (loadState) {
			is LoadState.Error -> ErrorState(
				onRetryClick = onRetryClick
			)

			LoadState.Loading -> CircularProgress()

			is LoadState.NotLoading -> {}
		}
	}
}

@TvPreview
@Composable
private fun ErrorState(
	onRetryClick: () -> Unit = {},
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = stringResource(R.string.error_mediathek_info_not_available),
			color = MaterialTheme.colorScheme.error,
			textAlign = TextAlign.Center,
		)

		Spacer(Modifier.height(16.dp))

		IconButton(
			onClick = onRetryClick,
		) {
			Icon(
				painter = painterResource(R.drawable.ic_refresh_white_24dp),
				contentDescription = stringResource(R.string.menu_retry)
			)
		}
	}
}
