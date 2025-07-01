package de.christinecoenen.code.zapp.tv2.live

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv2.theme.TvPreview

@TvPreview
@Composable
fun ChannelItem(
	name: String = "Das Erste",
	subtitle: String? = "My Subtitle",
	@DrawableRes logoResId: Int = R.drawable.channel_logo_das_erste,
	onClick: () -> Unit = {},
	onFocus: () -> Unit = {},
) {
	val hasSubtitle = !subtitle.isNullOrEmpty()
	Surface(
		shape = ClickableSurfaceDefaults.shape(CircleShape),
		colors = ClickableSurfaceDefaults.colors(
			focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
		),
		onClick = onClick,
		modifier = Modifier
            .size(100.dp)
            .onFocusChanged {
                if (it.hasFocus) {
                    onFocus()
                }
            }
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
			modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
		) {
			Image(
				painter = painterResource(logoResId),
				contentDescription = name,
				modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(bottom = if (hasSubtitle) 8.dp else 0.dp)
			)

			if (hasSubtitle) {
				Text(
					text = subtitle!!,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					style = MaterialTheme.typography.labelSmall,
				)
			}
		}
	}
}
