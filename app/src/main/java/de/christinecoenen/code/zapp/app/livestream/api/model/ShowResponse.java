package de.christinecoenen.code.zapp.app.livestream.api.model;


import androidx.annotation.NonNull;

import java.util.List;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "CanBeFinal"})
public class ShowResponse {

	private List<Show> shows;

	public Show getShow() {
		return shows.get(0);
	}

	public boolean isSuccess() {
		return shows != null && !shows.isEmpty();
	}

	@NonNull
	@Override
	public String toString() {
		return "ShowResponse{" +
			"shows=" + shows +
			'}';
	}
}
