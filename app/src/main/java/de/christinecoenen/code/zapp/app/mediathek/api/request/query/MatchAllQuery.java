package de.christinecoenen.code.zapp.app.mediathek.api.request.query;

public class MatchAllQuery extends Query {

	private final Object matchAll = new Object();

	@Override
	public String toString() {
		return "MatchAllQuery{" +
			"matchAll=" + matchAll +
			'}';
	}
}
