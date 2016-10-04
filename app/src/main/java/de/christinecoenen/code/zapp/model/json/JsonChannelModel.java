package de.christinecoenen.code.zapp.model.json;


import com.google.gson.annotations.SerializedName;

/**
 * Transitional model to store parsed json data.
 */
class JsonChannelModel {
	@SuppressWarnings("unused")
	@SerializedName("id")
	String id;

	@SuppressWarnings("unused")
	@SerializedName("name")
	String name;

	@SuppressWarnings("unused")
	@SerializedName("stream_url")
	String streamUrl;

	@SuppressWarnings("unused")
	@SerializedName("logo_name")
	String logoName;

	@SuppressWarnings("unused")
	@SerializedName("subtitle")
	String subtitle;

	@SuppressWarnings("unused")
	@SerializedName("color")
	String color;
}
