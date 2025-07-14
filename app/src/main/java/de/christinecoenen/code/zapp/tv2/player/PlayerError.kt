package de.christinecoenen.code.zapp.tv2.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun PlayerError(
	errorResId: Int? = R.string.error_stream_not_in_unmetered_network
) {
	if (errorResId == null || errorResId == -1) {
		return
	}

	// TODO: style

	AppTheme {
		Text(
			text = stringResource(errorResId),
			color = MaterialTheme.colorScheme.error,
			style = MaterialTheme.typography.headlineLarge,
		)
	}
}
