package de.christinecoenen.code.zapp.models.channels.json

import com.google.gson.annotations.SerializedName

/**
 * Transitional model to store parsed json data.
 */
internal data class JsonChannelModel(
	@SerializedName("id")
	var id: String,

	@SerializedName("name")
	var name: String,

	@SerializedName("stream_url")
	var streamUrl: String,

	@SerializedName("logo_name")
	var logoName: String,

	@SerializedName("subtitle")
	var subtitle: String? = null,

	@SerializedName("color")
	var color: String
)
