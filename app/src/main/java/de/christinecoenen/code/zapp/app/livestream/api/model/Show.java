package de.christinecoenen.code.zapp.app.livestream.api.model;

import androidx.annotation.NonNull;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import de.christinecoenen.code.zapp.app.livestream.model.LiveShow;

@SuppressWarnings({"unused", "CanBeFinal"})
public class Show {

	private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();

	private String title;
	private String subtitle;
	private String description;
	private String startTime;
	private String endTime;

	public LiveShow toLiveShow() {
		LiveShow liveShow = new LiveShow();
		liveShow.setTitle(title);
		liveShow.setSubtitle(subtitle);
		liveShow.setDescription(description);

		if (startTime != null && endTime != null) {
			liveShow.setStartTime(formatter.parseDateTime(startTime));
			liveShow.setEndTime(formatter.parseDateTime(endTime));
		}

		return liveShow;
	}

	@NonNull
	@Override
	public String toString() {
		return "Show{" +
			"title='" + title + '\'' +
			", subtitle='" + subtitle + '\'' +
			", description='" + description + '\'' +
			", startTime=" + startTime +
			", endTime=" + endTime +
			'}';
	}
}
