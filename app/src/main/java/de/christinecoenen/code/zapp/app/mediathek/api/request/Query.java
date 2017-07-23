package de.christinecoenen.code.zapp.app.mediathek.api.request;


import java.util.ArrayList;
import java.util.List;

class Query {

	private final List<String> fields = new ArrayList<>();
	private final String query;

	Query(String fieldName, String queryString) {
		fields.add(fieldName);
		query = queryString;
	}

	@Override
	public String toString() {
		return "Query{" +
			"fields=" + fields +
			", query='" + query + '\'' +
			'}';
	}
}
