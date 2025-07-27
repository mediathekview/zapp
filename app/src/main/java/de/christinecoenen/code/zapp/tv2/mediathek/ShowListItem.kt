package de.christinecoenen.code.zapp.tv2.mediathek

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun ShowListItem(
	title: String = "My title",
	topic: String = "My topic",
	duration: String = "1h 28m",
	chanel: String = "ZDF",
	releasedAt: String = "3 hours ago",
	selected: Boolean = false,
	onClick: () -> Unit = {},
) {
	AppTheme {
		val topicColorUnfocused = MaterialTheme.colorScheme.primary
		val topicColorFocused = MaterialTheme.colorScheme.inversePrimary

		var isFocused by remember { mutableStateOf(false) }

		val topicColor by remember {
			derivedStateOf {
				if (isFocused) topicColorFocused else topicColorUnfocused
			}
		}

		ListItem(
			selected = selected,
			onClick = onClick,
			colors = ListItemDefaults.colors(
				focusedContainerColor = MaterialTheme.colorScheme.inverseSurface,
			),
			overlineContent = {
				Text(
					text = topic,
					color = topicColor,
					style = MaterialTheme.typography.titleMedium,
				)
				Spacer(Modifier.height(4.dp))
			},
			headlineContent = {
				Text(
					text = title,
					style = MaterialTheme.typography.titleLarge
				)

				Spacer(Modifier.height(4.dp))
			},
			supportingContent = {
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = duration,
						style = MaterialTheme.typography.labelLarge
					)

					Text("|")

					Text(
						text = chanel,
						style = MaterialTheme.typography.labelLarge
					)

					Text("|")

					Text(
						text = releasedAt,
						style = MaterialTheme.typography.labelLarge
					)
				}
			},
			modifier = Modifier.onFocusChanged { isFocused = it.hasFocus }
		)
	}
}
