package de.christinecoenen.code.zapp.app.mediathek.api.request.query;

import com.google.gson.annotations.SerializedName;

public enum Field {

	@SerializedName("timestamp")
	TIMESTAMP,

	@SerializedName("topic")
	TOPIC,

	@SerializedName("title")
	TITLE,

	@SerializedName("channel")
	CHANNEL,

	@SerializedName("duration")
	DURATION

}
