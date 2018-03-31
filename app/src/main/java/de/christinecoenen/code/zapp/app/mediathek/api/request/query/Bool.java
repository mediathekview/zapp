package de.christinecoenen.code.zapp.app.mediathek.api.request.query;

import java.util.Arrays;
import java.util.List;

public class Bool {

	private List<Query> must;
	private List<Query> should;
	private List<Query> not;
	private List<Query> filter;

	public void setMustQueries(Query... queries) {
		must = Arrays.asList(queries);
	}

	public void setShouldQuery(Query... queries) {
		should = Arrays.asList(queries);
	}

	public void setNotQuery(Query... queries) {
		not = Arrays.asList(queries);
	}

	public void setFilterQuery(Query... queries) {
		filter = Arrays.asList(queries);
	}

	@Override
	public String toString() {
		return "Bool{" +
			"must=" + must +
			", should=" + should +
			", not=" + not +
			", filter=" + filter +
			'}';
	}
}
