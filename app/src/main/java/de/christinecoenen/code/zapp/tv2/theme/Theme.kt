package de.christinecoenen.code.zapp.tv2.theme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/**
 * This theme has bee generated with [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)
 * with following Parameters:
 * - Stay true to my color inputs
 * - Primary: #E1007A
 * - Secondary: #F44F17
 * - Tertiary: #B00050
 * - Error: #FF5449
 * - Neutral: #F5F5F5
 * - Neutral Variant: #E3E3E3
 */

private val darkScheme = darkColorScheme(
	primary = primaryDark,
	onPrimary = onPrimaryDark,
	primaryContainer = primaryContainerDark,
	onPrimaryContainer = onPrimaryContainerDark,
	secondary = secondaryDark,
	onSecondary = onSecondaryDark,
	secondaryContainer = secondaryContainerDark,
	onSecondaryContainer = onSecondaryContainerDark,
	tertiary = tertiaryDark,
	onTertiary = onTertiaryDark,
	tertiaryContainer = tertiaryContainerDark,
	onTertiaryContainer = onTertiaryContainerDark,
	error = errorDark,
	onError = onErrorDark,
	errorContainer = errorContainerDark,
	onErrorContainer = onErrorContainerDark,
	background = backgroundDark,
	onBackground = onBackgroundDark,
	surface = surfaceDark,
	onSurface = onSurfaceDark,
	surfaceVariant = surfaceVariantDark,
	onSurfaceVariant = onSurfaceVariantDark,
	scrim = scrimDark,
	inverseSurface = inverseSurfaceDark,
	inverseOnSurface = inverseOnSurfaceDark,
	inversePrimary = inversePrimaryDark,
)

@Composable
fun AppTheme(
	content: @Composable () -> Unit
) {
	MaterialTheme(
		colorScheme = darkScheme,
		typography = AppTypography,
		content = content
	)
}

@Preview(name = "TV Preview", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class TvPreview
