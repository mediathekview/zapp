package de.christinecoenen.code.zapp.model

import android.content.Intent
import android.graphics.Color
import android.net.Uri

import java.io.Serializable

data class ChannelModel(var id: String = "",
						var name: String = "",
						var subtitle: String? = null,
						var streamUrl: String = "",
						var drawableId: Int = 0,
						var color: Int = Color.BLACK) : Serializable {

	val videoShareIntent: Intent
		get() {
			val videoIntent = Intent(Intent.ACTION_VIEW)
			videoIntent.setDataAndType(Uri.parse(streamUrl), "video/*")
			return videoIntent
		}
}
