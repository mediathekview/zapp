package de.christinecoenen.code.zapp.app.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket;
import de.christinecoenen.code.zapp.model.ChannelModel;

public class VideoInfo {

	public static VideoInfo fromShow(PersistedMediathekShow persistedShow) {
		MediathekShow show = persistedShow.getMediathekShow();

		VideoInfo videoInfo = new VideoInfo();
		videoInfo.id = persistedShow.getId();
		videoInfo.url = show.getVideoUrl(Quality.Medium);
		videoInfo.urlHighestQuality = show.getVideoUrl(Quality.High);
		videoInfo.urlLowestQuality = show.getVideoUrl(Quality.Low);
		videoInfo.filePath = persistedShow.getDownloadedVideoPath();
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

	private int id = 0;

	@NonNull
	private String url = "";

	private String urlLowestQuality = null;

	private String urlHighestQuality = null;

	@NonNull
	private String title = "";

	private String subtitle;

	@Nullable
	private String subtitleUrl;

	private String filePath;

	private boolean hasDuration = false;

	public int getId() {
		return id;
	}

	@NonNull
	String getPlaybackUrlOrFilePath(StreamQualityBucket quality) {
		if (isOfflineVideo()) {
			return filePath;
		}

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
		// no subtitle support for downloaded videos yet
		return subtitleUrl != null && !isOfflineVideo();
	}

	public boolean isOfflineVideo() {
		return filePath != null;
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
			Objects.equals(filePath, videoInfo.filePath) &&
			Objects.equals(subtitleUrl, videoInfo.subtitleUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hash(url, urlLowestQuality, urlHighestQuality, title, subtitle, subtitleUrl, filePath, hasDuration);
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
			", filePath='" + filePath + '\'' +
			", hasDuration=" + hasDuration +
			'}';
	}
}
