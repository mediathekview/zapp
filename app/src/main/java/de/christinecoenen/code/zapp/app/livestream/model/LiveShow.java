package de.christinecoenen.code.zapp.app.livestream.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class LiveShow {

	private String title;
	private String subtitle;
	private String description;
	private DateTime startTime;
	private DateTime endTime;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	@SuppressWarnings("unused")
	public void setDescription(String description) {
		this.description = description;
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}

	public DateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(DateTime endTime) {
		this.endTime = endTime;
	}

	public boolean hasDuration() {
		return startTime != null && endTime != null;
	}

	public float getProgressPercent() {
		Duration showDuration = new Duration(startTime, endTime);
		Duration runningDuration = new Duration(startTime, DateTime.now());
		return (float) runningDuration.getStandardSeconds() / showDuration.getStandardSeconds();
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
