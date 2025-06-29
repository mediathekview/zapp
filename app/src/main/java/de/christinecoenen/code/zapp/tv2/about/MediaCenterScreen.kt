package de.christinecoenen.code.zapp.tv2.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun MediaCenterScreen() {
	Column {
		listOf(1, 2, 3, 4, 5).forEach {
			Button(
				onClick = {}
			) {
				Text(it.toString())
			}
		}
	}
}
