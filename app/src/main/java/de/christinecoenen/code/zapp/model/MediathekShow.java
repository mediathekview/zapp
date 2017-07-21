package de.christinecoenen.code.zapp.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@SuppressWarnings("unused")
public class MediathekShow implements Serializable {

	private String id;
	private String topic;
	private String title;
	private String description;
	private String channel;
	private int timestamp;
	private int size;
	private int duration;
	private int filmlisteTimestamp;

	@SerializedName("url_website")
	private String websiteUrl;

	@SerializedName("url_subtitle")
	private String subtitleUrl;

	@SerializedName("url_video")
	private String videoUrl;

	@SerializedName("url_video_low")
	private String videoUrlLow;

	@SerializedName("url_video_hd")
	private String videoUrlHd;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getFilmlisteTimestamp() {
		return filmlisteTimestamp;
	}

	public void setFilmlisteTimestamp(int filmlisteTimestamp) {
		this.filmlisteTimestamp = filmlisteTimestamp;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}

	public String getSubtitleUrl() {
		return subtitleUrl;
	}

	public void setSubtitleUrl(String subtitleUrl) {
		this.subtitleUrl = subtitleUrl;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getVideoUrlLow() {
		return videoUrlLow;
	}

	public void setVideoUrlLow(String videoUrlLow) {
		this.videoUrlLow = videoUrlLow;
	}

	public String getVideoUrlHd() {
		return videoUrlHd;
	}

	public void setVideoUrlHd(String videoUrlHd) {
		this.videoUrlHd = videoUrlHd;
	}

	@Override
	public String toString() {
		return "MediathekShow{" +
			"id='" + id + '\'' +
			", topic='" + topic + '\'' +
			", title='" + title + '\'' +
			", description='" + description + '\'' +
			", channel='" + channel + '\'' +
			", timestamp=" + timestamp +
			", size=" + size +
			", duration=" + duration +
			", filmlisteTimestamp=" + filmlisteTimestamp +
			", websiteUrl='" + websiteUrl + '\'' +
			", subtitleUrl='" + subtitleUrl + '\'' +
			", videoUrl='" + videoUrl + '\'' +
			", videoUrlLow='" + videoUrlLow + '\'' +
			", videoUrlHd='" + videoUrlHd + '\'' +
			'}';
	}
}
