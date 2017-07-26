package de.christinecoenen.code.zapp.app.mediathek.api.request;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Query implements Serializable {

	private final List<String> fields = new ArrayList<>();
	private final String query;

	Query(String queryString, String... fieldNames) {
		Collections.addAll(fields, fieldNames);
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
