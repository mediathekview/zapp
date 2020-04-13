package de.christinecoenen.code.zapp.app.livestream.api.model;


import androidx.annotation.NonNull;

@SuppressWarnings({"unused", "CanBeFinal"})
public class ChannelInfo {

	private String streamUrl;

	public String getStreamUrl() {
		return streamUrl;
	}

	@NonNull
	@Override
	public String toString() {
		return "ChannelInfoResponse{" +
			"streamUrl='" + streamUrl + '\'' +
			'}';
	}
}
