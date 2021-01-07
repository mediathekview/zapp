package de.christinecoenen.code.zapp.models.channels

import android.content.Intent
import android.net.Uri
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

	val videoShareIntent
		get() = Intent(Intent.ACTION_VIEW).apply {
			setDataAndType(Uri.parse(streamUrl), "video/*")
		}
}
