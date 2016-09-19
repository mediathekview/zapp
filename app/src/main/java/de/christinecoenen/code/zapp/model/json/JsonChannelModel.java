package de.christinecoenen.code.zapp.model.json;


import com.google.gson.annotations.SerializedName;

/**
 * Transitional model to store parsed json data.
 */
class JsonChannelModel {
	@SerializedName("id")
	public String id;

	@SerializedName("name")
	public String name;

	@SerializedName("stream_url")
	public String streamUrl;

	@SerializedName("logo_name")
	public String logoName;
}
