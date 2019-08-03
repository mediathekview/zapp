package de.christinecoenen.code.zapp.app.livestream.api.model;


@SuppressWarnings({"unused", "CanBeFinal"})
public class ChannelInfo {

	private String streamUrl;

	public String getStreamUrl() {
		return streamUrl;
	}

	@Override
	public String toString() {
		return "ChannelInfoResponse{" +
			"streamUrl='" + streamUrl + '\'' +
			'}';
	}
}
