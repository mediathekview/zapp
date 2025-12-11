package de.christinecoenen.code.zapp.tv2.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv2.main.navigation.MainScreenLocation
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import de.christinecoenen.code.zapp.tv2.theme.TvScreenPreview
import kotlinx.serialization.Serializable

@Serializable
data object AboutScreenLocation : MainScreenLocation(
	titleResId = R.string.menu_about_short,
)

@TvScreenPreview
@Composable
fun AboutScreen() {
	AppTheme {
		Icon(
			painter = painterResource(R.drawable.ic_zapp_tv),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.surfaceVariant,
			modifier = Modifier
				.alpha(0.06f)
				.scale(1.3f)
				.padding(top = 20.dp)
				.fillMaxSize()
		)
		Row(
			horizontalArrangement = Arrangement.spacedBy(92.dp, Alignment.CenterHorizontally),
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.padding(horizontal = 58.dp)
				.padding(top = 52.dp)
				.fillMaxSize()
		) {
			Text(
				text = stringResource(R.string.about_summary),
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.width(268.dp)
			)
			AboutMenu()
		}
	}
}
