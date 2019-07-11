package de.christinecoenen.code.zapp.app.player;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.model.ChannelModel;

public class VideoInfo {

	public static VideoInfo fromShow(MediathekShow show) {
		VideoInfo videoInfo = new VideoInfo();
		videoInfo.url = show.getVideoUrl();
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

	@NonNull
	private String title = "";

	@NonNull
	private String subtitle = "";

	@Nullable
	private String subtitleUrl;

	private boolean hasDuration = false;

	@NonNull
	public String getUrl() {
		return url;
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
	public String getSubtitleUrl() {
		return subtitleUrl;
	}

	public boolean hasSubtitles() {
		return subtitleUrl != null;
	}

	public boolean hasDuration() {
		return hasDuration;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VideoInfo videoInfo = (VideoInfo) o;
		return Objects.equals(url, videoInfo.url) &&
			Objects.equals(title, videoInfo.title) &&
			Objects.equals(subtitle, videoInfo.subtitle) &&
			hasDuration == videoInfo.hasDuration;
	}

	@NonNull
	@Override
	public String toString() {
		return "VideoInfo{" +
			"url='" + url + '\'' +
			", title='" + title + '\'' +
			", subtitle='" + subtitle + '\'' +
			", subtitleUrl='" + subtitleUrl + '\'' +
			", hasDuration=" + hasDuration +
			'}';
	}
}
