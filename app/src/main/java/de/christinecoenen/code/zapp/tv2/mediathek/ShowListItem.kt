package de.christinecoenen.code.zapp.tv2.mediathek

import androidx.compose.runtime.Composable
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun ShowListItem(
	title: String = "My title",
	topic: String = "My topic",
	selected: Boolean = false,
	onClick: () -> Unit = {},
) {
	ListItem(
		selected = selected,
		onClick = onClick,
		headlineContent = {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium
			)
		},
		supportingContent = {
			Text(
				text = topic,
				style = MaterialTheme.typography.titleSmall
			)
		}
	)
}
