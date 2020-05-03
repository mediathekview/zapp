package de.christinecoenen.code.zapp.app.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket;
import de.christinecoenen.code.zapp.model.ChannelModel;

public class VideoInfo {

	public static VideoInfo fromShow(MediathekShow show) {
		VideoInfo videoInfo = new VideoInfo();
		videoInfo.url = show.getVideoUrl(Quality.Medium);
		videoInfo.urlHighestQuality = show.getVideoUrl(Quality.High);
		videoInfo.urlLowestQuality = show.getVideoUrl(Quality.Low);
		videoInfo.title = show.getTitle();
		videoInfo.subtitle = show.getTopic();
		videoInfo.subtitleUrl = show.getSubtitleUrl();
		videoInfo.hasDuration = true;
		return videoInfo;
	}

	public static VideoInfo fromChannel(ChannelModel channel) {
		VideoInfo videoInfo = new VideoInfo();
		videoInfo.url = channel.getStreamUrl();
		videoInfo.title = channel.getName();
		videoInfo.subtitle = channel.getSubtitle();
		videoInfo.hasDuration = false;
		return videoInfo;
	}

	@NonNull
	private String url = "";

	private String urlLowestQuality = null;

	private String urlHighestQuality = null;

	@NonNull
	private String title = "";

	private String subtitle;

	@Nullable
	private String subtitleUrl;

	private boolean hasDuration = false;

	@NonNull
	String getUrl() {
		return url;
	}

	@NonNull
	String getUrl(StreamQualityBucket quality) {
		switch (quality) {
			case MEDIUM:
				return url;
			case HIGHEST:
				return urlHighestQuality == null ? url : urlHighestQuality;
			default:
				return urlLowestQuality == null ? url : urlLowestQuality;
		}
	}

	@NonNull
	public String getTitle() {
		return title;
	}

	@NonNull
	public String getSubtitle() {
		return subtitle;
	}

	@Nullable
	String getSubtitleUrl() {
		return subtitleUrl;
	}

	public boolean hasSubtitles() {
		return subtitleUrl != null;
	}

	boolean hasDuration() {
		return hasDuration;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VideoInfo videoInfo = (VideoInfo) o;
		return hasDuration == videoInfo.hasDuration &&
			url.equals(videoInfo.url) &&
			Objects.equals(urlLowestQuality, videoInfo.urlLowestQuality) &&
			Objects.equals(urlHighestQuality, videoInfo.urlHighestQuality) &&
			title.equals(videoInfo.title) &&
			Objects.equals(subtitle, videoInfo.subtitle) &&
			Objects.equals(subtitleUrl, videoInfo.subtitleUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hash(url, urlLowestQuality, urlHighestQuality, title, subtitle, subtitleUrl, hasDuration);
	}

	@NonNull
	@Override
	public String toString() {
		return "VideoInfo{" +
			"url='" + url + '\'' +
			", urlLowestQuality='" + urlLowestQuality + '\'' +
			", urlHighestQuality='" + urlHighestQuality + '\'' +
			", title='" + title + '\'' +
			", subtitle='" + subtitle + '\'' +
			", subtitleUrl='" + subtitleUrl + '\'' +
			", hasDuration=" + hasDuration +
			'}';
	}
}
