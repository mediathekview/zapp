package de.christinecoenen.code.zapp.tv2.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun ControllerOverlay(
	title: String = "My Test Title",
	subtitle: String? = "My Test Subtitle",
	isVisible: Boolean = true,
) {
	if (!isVisible) {
		return
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.scrim)
	) {
		Text(
			text = title,
			color = MaterialTheme.colorScheme.onSurface,
			style = MaterialTheme.typography.headlineLarge,
		)

		if (subtitle != null) {
			Text(
				text = subtitle,
				color = MaterialTheme.colorScheme.onSurface,
				style = MaterialTheme.typography.headlineMedium,
			)
		}
	}
}
