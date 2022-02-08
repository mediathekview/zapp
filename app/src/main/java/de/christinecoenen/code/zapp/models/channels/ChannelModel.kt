package de.christinecoenen.code.zapp.models.channels

import android.content.Context
import de.christinecoenen.code.zapp.utils.system.IntentHelper
import java.io.Serializable

data class ChannelModel(
	val id: String,
	val name: String,
	val subtitle: String?,
	var streamUrl: String,
	val drawableId: Int,
	val color: Int,
	var isEnabled: Boolean = true

) : Serializable {

	fun toggleIsEnabled() {
		isEnabled = !isEnabled
	}

	fun playExternally(context: Context) {
		IntentHelper.playVideo(context, streamUrl, name)
	}
}
