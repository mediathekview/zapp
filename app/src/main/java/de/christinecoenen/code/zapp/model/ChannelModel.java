package de.christinecoenen.code.zapp.model;

public class ChannelModel {

	private String name;
	private String streamUrl;
	private int drawableId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStreamUrl() {
		return streamUrl;
	}

	public void setStreamUrl(String streamUrl) {
		this.streamUrl = streamUrl;
	}

	public int getDrawableId() {
		return drawableId;
	}

	public void setDrawableId(int drawableId) {
		this.drawableId = drawableId;
	}

	@Override
	public String toString() {
		return "ChannelModel{" +
				"name='" + name + '\'' +
				", streamUrl='" + streamUrl + '\'' +
				", drawableId=" + drawableId +
				'}';
	}
}
