package de.christinecoenen.code.programguide.model;

import org.joda.time.DateTime;

public class Show {

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
