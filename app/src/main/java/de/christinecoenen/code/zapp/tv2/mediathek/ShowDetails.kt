package de.christinecoenen.code.zapp.tv2.mediathek

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun ShowDetails(
	modifier: Modifier = Modifier,
	title: String = "My title",
	topic: String = "My topic",
	description: String = "My longer description to show here."
) {
	Column(
		modifier = modifier
	) {
		Text(
			topic,
			color = MaterialTheme.colorScheme.onSurface,
			style = MaterialTheme.typography.titleSmall,
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			title,
			color = MaterialTheme.colorScheme.onSurface,
			style = MaterialTheme.typography.titleLarge,
		)
		Spacer(modifier = Modifier.height(8.dp))

		Text(
			description,
			color = MaterialTheme.colorScheme.onSurface,
			style = MaterialTheme.typography.bodyMedium,
		)
		Spacer(modifier = Modifier.height(16.dp))

		Button(
			onClick = {},
			contentPadding = ButtonDefaults.ButtonWithIconContentPadding
		) {
			Icon(
				Icons.Filled.PlayArrow,
				contentDescription = null,
				modifier = Modifier.size(ButtonDefaults.IconSize)
			)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Text(stringResource(R.string.action_play))
		}
	}
}
