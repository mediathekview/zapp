package de.christinecoenen.code.zapp.app.livestream.api.model;


import java.util.List;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused", "CanBeFinal"})
public class ShowResponse {

	private List<Show> shows;

	public Show getShow() {
		return shows.get(0);
	}

	public boolean isSuccess() {
		return shows != null && !shows.isEmpty();
	}

	@Override
	public String toString() {
		return "ShowResponse{" +
			"shows=" + shows +
			'}';
	}
}
