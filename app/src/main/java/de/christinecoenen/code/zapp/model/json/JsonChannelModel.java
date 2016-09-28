package de.christinecoenen.code.zapp.model.json;


import com.google.gson.annotations.SerializedName;

/**
 * Transitional model to store parsed json data.
 */
class JsonChannelModel {
	@SuppressWarnings("unused")
	@SerializedName("id")
	public String id;

	@SuppressWarnings("unused")
	@SerializedName("name")
	public String name;

	@SuppressWarnings("unused")
	@SerializedName("stream_url")
	public String streamUrl;

	@SuppressWarnings("unused")
	@SerializedName("logo_name")
	public String logoName;

	@SuppressWarnings("unused")
	@SerializedName("subtitle")
	public String subtitle;
}
