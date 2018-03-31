package de.christinecoenen.code.zapp.app.mediathek.api.request.query;

public class BoolQuery extends Query {

	private Bool bool = new Bool();

	public BoolQuery setMustQueries(Query... queries) {
		bool.setMustQueries(queries);
		return this;
	}

	public BoolQuery setShouldQueries(Query... queries) {
		bool.setShouldQuery(queries);
		return this;
	}

	public BoolQuery setNotQueries(Query... queries) {
		bool.setNotQuery(queries);
		return this;
	}

	public BoolQuery setFilterQueries(Query... queries) {
		bool.setFilterQuery(queries);
		return this;
	}

	@Override
	public String toString() {
		return "BoolQuery{" +
			"bool=" + bool +
			'}';
	}
}
