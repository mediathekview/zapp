package de.christinecoenen.code.programguide.model;

import android.content.Context;
import android.text.format.DateUtils;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import de.christinecoenen.code.programguide.R;

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

	public boolean hasDuration() {
		return startTime != null && endTime != null;
	}

	public String getTimeString(Context context) {
		if (!hasDuration()) {
			return "";
		}

		String startTimeString = DateUtils.formatDateTime(context,
			startTime.getMillis(), DateUtils.FORMAT_SHOW_TIME);
		String endTimeString = DateUtils.formatDateTime(context,
			endTime.getMillis(), DateUtils.FORMAT_SHOW_TIME);

		return context.getString(R.string.program_info_show_time, startTimeString, endTimeString);
	}

	public float getProgressPercent() {
		Duration showDuration = new Duration(startTime, endTime);
		Duration runningDuration = new Duration(startTime, DateTime.now());
		return (float) runningDuration.getStandardSeconds() / showDuration.getStandardSeconds();
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
