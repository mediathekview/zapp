package de.christinecoenen.code.zapp.tv2.live

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import de.christinecoenen.code.zapp.tv2.theme.TvPreview


@TvPreview
@Composable
fun ChannelInfo(
	modifier: Modifier = Modifier,
	showTitle: String = "My show",
	showSubtitle: String? = "With subtitle",
	description: String? = "A <b>description</b> with many, many words.",
	time: String? = "20:15 - 21 Uhr",
	progressPercent: Float? = 0.25f,
) {
	AppTheme {
		Column(
			modifier = modifier,
		) {
			if (time != null) {
				Text(
					text = time,
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				Spacer(Modifier.height(8.dp))
			}

			Text(
				text = showTitle,
				style = MaterialTheme.typography.headlineLarge,
				color = MaterialTheme.colorScheme.onSurface,
			)

			if (!showSubtitle.isNullOrEmpty()) {
				Spacer(Modifier.height(8.dp))

				Text(
					text = showSubtitle,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}

			if (!description.isNullOrEmpty()) {
				Spacer(Modifier.height(16.dp))

				Text(
					text = AnnotatedString.Companion.fromHtml(description),
					maxLines = 4,
					overflow = TextOverflow.Ellipsis,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}

			// TODO: display duration
		}
	}
}
