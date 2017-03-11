package de.christinecoenen.code.programguide.plugins.ard;


import android.util.ArrayMap;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;

class ArdParser {

	private static final String TAG = ArdParser.class.getSimpleName();

	// 20:15
	private static final DateTimeFormatter TIME_FORMATTER =
			DateTimeFormat.forPattern("HH:mm");

	private static final DateTimeZone TIME_ZONE_GERMANY = DateTimeZone.forID("Europe/Berlin");

	private static final Pattern TIME_PATTERN = Pattern.compile("\\d+:\\d+");

	static Map<Channel, Show> parse(String html) {
		Map<Channel, Show> shows = new ArrayMap<>();

		Document document = Jsoup.parseBodyFragment(html);
		Elements items = document.select("li a");

		for (Element item : items) {
			shows.putAll(parseItem(item));
		}

		return shows;
	}

	private static Map<Channel, Show> parseItem(Element item) {
		String title = item.select("b").text();
		String rawMetadata = item.select("span.small").text();

		Map<Channel, Show> shows = new ArrayMap<>();
		Show show = parseShow(title, rawMetadata);

		if (show.getProgressPercent() <= 1) {
			// show is still up to date - fill map
			List<Channel> channels = getChannels(rawMetadata);
			for (Channel channel : channels) {
				shows.put(channel, show);
			}
		}

		return shows;
	}

	private static Show parseShow(String title, String rawMetadata) {
		Show show = new Show();
		show.setTitle(title);
		setTimes(show, rawMetadata);

		Log.d(TAG, show.toString());
		return show;
	}

	private static void setTimes(Show show, String rawMetadata) {
		String[] splitMetadata = rawMetadata.split("\\|"); // MDR Fernsehen | 19:30 - 19:50 Uhr
		String rawTime = splitMetadata[splitMetadata.length - 1]; // 19:30 - 19:50 Uhr

		Matcher matcher = TIME_PATTERN.matcher(rawTime);

		if (matcher.find()) {
			DateTime startTime = TIME_FORMATTER.parseDateTime(matcher.group());
			MutableDateTime startTimeDate = MutableDateTime.now(TIME_ZONE_GERMANY);
			startTimeDate.setMillisOfDay(0);
			startTimeDate.setMinuteOfDay(startTime.getMinuteOfDay());
			show.setStartTime(startTimeDate.toDateTime());
		}

		if (matcher.find()) {
			DateTime endTime = TIME_FORMATTER.parseDateTime(matcher.group());
			MutableDateTime endTimeDate = MutableDateTime.now(TIME_ZONE_GERMANY);
			endTimeDate.setMillisOfDay(0);
			endTimeDate.setMinuteOfDay(endTime.getMinuteOfDay());
			show.setEndTime(endTimeDate.toDateTime());

			if (show.getEndTime().isBefore(show.getStartTime())) {
				// show plays around midnight
				if (DateTime.now(TIME_ZONE_GERMANY).getHourOfDay() < 12) {
					// now is morning - startTime was yesterday
					DateTime newStartTime = show.getStartTime().minusDays(1);
					show.setStartTime(newStartTime);
				} else {
					// now is evening - end time is tomorrow
					DateTime newEndTime = show.getEndTime().plusDays(1);
					show.setEndTime(newEndTime);
				}
			}
		}
	}

	private static List<Channel> getChannels(String rawMetadata) {
		// SR Fernsehen | SWR Ferns. BW | SWR Ferns. RP | 20:15 - 21:00 Uhr
		String[] splitMetadata = rawMetadata.split("\\|");

		List<Channel> channels = new ArrayList<>();
		for (int i = 0; i < splitMetadata.length-1; i++) {
			Channel[] parsedChannels = getParsedChannels(splitMetadata[i]);
			if (parsedChannels != null) {
				channels.addAll(Arrays.asList(parsedChannels));
			}
		}

		return channels;
	}

	private static Channel[] getParsedChannels(String htmlName) {
		switch(htmlName.trim()) {
			case "ARD-alpha":
				return new Channel[] { Channel.ARD_ALPHA };
			case "Das Erste":
				return new Channel[] { Channel.DAS_ERSTE };
			case "SR Fernsehen":
				return new Channel[] { Channel.SR };
			case "tagesschau24":
				return new Channel[] { Channel.TAGESSCHAU24 };
			case "SWR Ferns. BW":
				return new Channel[] { Channel.SWR_BADEN_WUERTTEMBERG };
			case "SWR Ferns. RP":
				return new Channel[] { Channel.SWR_RHEINLAND_PFALZ };
			case "WDR Fernsehen":
				return new Channel[] { Channel.WDR };
			case "ONE":
				return new Channel[] { Channel.ONE };
			case "hr-fernsehen":
				return new Channel[] { Channel.HR };
			case "BR Fernsehen":
				return new Channel[] {
						Channel.BR_NORD,
						Channel.BR_SUED
				};
			case "MDR Fernsehen":
				return new Channel[] {
						Channel.MDR_SACHSEN,
						Channel.MDR_SACHSEN_ANHALT,
						Channel.MDR_THUERINGEN
				};
			case "NDR Fernsehen":
				return new Channel[] {
						Channel.NDR_HAMBURG,
						Channel.NDR_MECKLENBURG_VORPOMMERN,
						Channel.NDR_NIEDERSACHSEN,
						Channel.NDR_SCHLESWIG_HOLSTEIN
				};
			case "rbb Fernsehen":
				return new Channel[] {
						Channel.RBB_BERLIN,
						Channel.RBB_BRANDENBURG
				};
			case "KiKA":
				return  new Channel[] {Channel.KIKA};
			case "EinsPlus": // ?
			default:
				return null;
		}
	}
}
