package de.christinecoenen.code.zapp.app.player;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.model.ChannelModel;

public class VideoInfo {

	public static VideoInfo fromShow(MediathekShow show) {
		VideoInfo videoInfo = new VideoInfo();
		videoInfo.url = show.getVideoUrl();
		videoInfo.title = show.getTitle();
		videoInfo.subtitle = show.getTopic();
		return videoInfo;
	}

	public static VideoInfo fromChannel(ChannelModel channel) {
		VideoInfo videoInfo = new VideoInfo();
		videoInfo.url = channel.getStreamUrl();
		videoInfo.title = channel.getName();
		videoInfo.subtitle = channel.getSubtitle();
		return videoInfo;
	}

	@NotNull
	private String url = "";

	@NotNull
	private String title = "";

	@NotNull
	private String subtitle = "";

	@NotNull
	public String getUrl() {
		return url;
	}

	@NotNull
	public String getTitle() {
		return title;
	}

	@NotNull
	public String getSubtitle() {
		return subtitle;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VideoInfo videoInfo = (VideoInfo) o;
		return Objects.equals(url, videoInfo.url) &&
			Objects.equals(title, videoInfo.title) &&
			Objects.equals(subtitle, videoInfo.subtitle);
	}

	@Override
	public String toString() {
		return "VideoInfo{" +
			"url='" + url + '\'' +
			", title='" + title + '\'' +
			", subtitle='" + subtitle + '\'' +
			'}';
	}
}
