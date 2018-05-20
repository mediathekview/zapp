package de.christinecoenen.code.zapp.app.mediathek.model;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.Serializable;
import java.util.List;


@SuppressWarnings("unused")
public class MediathekShow implements Serializable {

	private static final PeriodFormatter hourPeriodFormatter = new PeriodFormatterBuilder()
		.appendHours()
		.appendSeparatorIfFieldsBefore("h ")
		.appendMinutes()
		.appendSeparatorIfFieldsBefore("m")
		.toFormatter();

	private static final PeriodFormatter secondsPeriodFormatter = new PeriodFormatterBuilder()
		.appendMinutes()
		.appendSeparatorIfFieldsBefore("m ")
		.appendSeconds()
		.appendSeparatorIfFieldsBefore("s")
		.toFormatter();

	private String id;
	private String topic;
	private String title;
	private String description;
	private String channel;
	private int timestamp;
	private long duration;
	private String website;
	private List<MediathekMedia> media;


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

	public List<MediathekMedia> getMedia() {
		return media;
	}

	public CharSequence getFormattedTimestamp() {
		return DateUtils.getRelativeTimeSpanString(timestamp * DateUtils.SECOND_IN_MILLIS);
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public String getFormattedDuration() {
		Period period = Duration.standardSeconds(duration).toPeriod();
		PeriodFormatter formatter = (period.getHours() > 0) ? hourPeriodFormatter : secondsPeriodFormatter;
		return period.toString(formatter);
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public boolean hasSubtitle() {
		return getSubtitleMedia() != null;
	}

	public @NonNull
	String getHighestQualityVideoUrl() {
		MediathekMedia highestQualityMedia = null;
		for (MediathekMedia media : media) {
			if (highestQualityMedia == null ||
				highestQualityMedia.getQuality().ordinal() < media.getQuality().ordinal()) {
				highestQualityMedia = media;
			}
		}

		if (highestQualityMedia == null) {
			throw new RuntimeException("show has no media attached");
		} else {
			return highestQualityMedia.getUrl();
		}
	}

	public String getSubtitleUrl() {
		MediathekMedia subtitleMedia = getSubtitleMedia();
		return subtitleMedia == null ? null : subtitleMedia.getUrl();
	}

	public String getDownloadFileName(MediathekMedia media) {
		String extension = FilenameUtils.getExtension(media.getUrl());
		String fileName = title.replace("/", "-");
		return fileName + "." + extension;
	}

	public String getDownloadFileNameSubtitle() {
		return getDownloadFileName(getSubtitleMedia());
	}

	public Intent getShareIntentPlain() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, topic + " - " + title);
		intent.putExtra(Intent.EXTRA_TEXT, getHighestQualityVideoUrl());
		return intent;
	}

	private MediathekMedia getSubtitleMedia() {
		for (MediathekMedia media : media) {
			if (media.isSubtitle()) {
				return media;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MediathekShow that = (MediathekShow) o;

		return id.equals(that.id);

	}

	@Override
	public int hashCode() {
		return id.hashCode();
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
			", duration=" + duration +
			", website='" + website + '\'' +
			", media=" + media +
			'}';
	}
}
