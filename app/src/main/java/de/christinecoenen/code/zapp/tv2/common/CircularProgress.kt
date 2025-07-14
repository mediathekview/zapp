package de.christinecoenen.code.zapp.tv2.common

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun CircularProgress(
	modifier: Modifier = Modifier,
	indeterminate: Boolean = true,
	progress: () -> Float = { 0.7f }
) {
	AppTheme {
		if (indeterminate) {
			CircularProgressIndicator(
				color = MaterialTheme.colorScheme.primary,
				trackColor = MaterialTheme.colorScheme.onSurface,
				modifier = modifier,
			)
		} else {
			CircularProgressIndicator(
				progress = progress,
				color = MaterialTheme.colorScheme.primary,
				trackColor = MaterialTheme.colorScheme.onSurface,
				modifier = modifier,
			)
		}
	}
}
