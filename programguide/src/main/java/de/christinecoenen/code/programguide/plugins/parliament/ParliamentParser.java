package de.christinecoenen.code.programguide.plugins.parliament;


import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import de.christinecoenen.code.programguide.model.Show;

class ParliamentParser {

	@SuppressWarnings("unused")
	private static final String TAG = ParliamentParser.class.getSimpleName();

	static Show parse(String xml) {
		Document document = Jsoup.parse(xml, "", Parser.xmlParser());
		Elements showNodes = document.select("sendung");

		for (Element show : showNodes) {
			// is it the right show?
			String startUnix = show.select("anfangUnix").text();
			String endUnix = show.select("endeUnix").text();
			DateTime start = new DateTime(Long.parseLong(startUnix) * 1000);
			DateTime end = new DateTime(Long.parseLong(endUnix) * 1000);

			if (start.isBeforeNow() && end.isAfterNow()) {
				return parseShow(show, start, end);
			}
		}

		// may happen in the time between shows
		return Show.getIntermission();
	}

	private static Show parseShow(Element showElement, DateTime start, DateTime end) {
		String title = showElement.select("langtitel").text();
		String subtitle;

		Element liveElement = showElement.select("live").first();
		if (liveElement.hasText()) {
			subtitle = liveElement.text();
		} else {
			subtitle = "Aufzeichnung vom " + showElement.select("aufzeichnungsdatum").text();
		}

		Show show = new Show();
		show.setTitle(title);
		show.setSubtitle(subtitle);
		show.setStartTime(start);
		show.setEndTime(end);

		return show;
	}
}
