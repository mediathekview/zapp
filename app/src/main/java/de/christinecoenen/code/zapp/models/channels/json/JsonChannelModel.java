package de.christinecoenen.code.zapp.models.channels.json;


import com.google.gson.annotations.SerializedName;

/**
 * Transitional model to store parsed json data.
 */
@SuppressWarnings({"CanBeFinal"})
class JsonChannelModel {
	@SerializedName("id")
	String id;

	@SerializedName("name")
	String name;

	@SerializedName("stream_url")
	String streamUrl;

	@SerializedName("logo_name")
	String logoName;

	@SerializedName("subtitle")
	String subtitle;

	@SerializedName("color")
	String color;
}
