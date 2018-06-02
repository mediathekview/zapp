package de.christinecoenen.code.zapp.model.json


import com.google.gson.annotations.SerializedName

/**
 * Transitional model to store parsed json data.
 */
internal data class JsonChannelModel(

	@SerializedName("id")
	val id: String,

	@SerializedName("name")
	val name: String,

	@SerializedName("stream_url")
	val streamUrl: String,

	@SerializedName("logo_name")
	val logoName: String,

	@SerializedName("subtitle")
	val subtitle: String? = null,

	@SerializedName("color")
	val color: String

)
