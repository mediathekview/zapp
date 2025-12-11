package de.christinecoenen.code.zapp.tv2.main.navigation

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
open class MainScreenLocation(
	@StringRes
	val titleResId: Int,
) : NavKey
