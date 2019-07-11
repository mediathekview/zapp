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
		return videoInfo;
	}

	public static VideoInfo fromChannel(ChannelModel channel) {
		VideoInfo videoInfo = new VideoInfo();
		videoInfo.url = channel.getStreamUrl();
		videoInfo.title = channel.getName();
		videoInfo.subtitle = channel.getSubtitle();
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VideoInfo videoInfo = (VideoInfo) o;
		return Objects.equals(url, videoInfo.url) &&
			Objects.equals(title, videoInfo.title) &&
			Objects.equals(subtitle, videoInfo.subtitle);
	}

	@NonNull
	@Override
	public String toString() {
		return "VideoInfo{" +
			"url='" + url + '\'' +
			", title='" + title + '\'' +
			", subtitle='" + subtitle + '\'' +
			", subtitleUrl='" + subtitleUrl + '\'' +
			'}';
	}
}
