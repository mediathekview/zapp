package de.christinecoenen.code.zapp.app.mediathek.api.request;


import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class QueryRequest {

	private final List<Query> queries = new ArrayList<>();

	private String sortBy = "timestamp";
	private String sortOrder = "desc";

	@SuppressWarnings("FieldCanBeLocal")
	private final boolean future = false;
	private int offset = 0;
	private int size = 10;

	public QueryRequest addQuery(String fieldName, String queryString) {
		this.queries.add(new Query(fieldName, queryString));
		return this;
	}

	public QueryRequest setSortBy(String sortBy) {
		this.sortBy = sortBy;
		return this;
	}

	public QueryRequest setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
		return this;
	}

	public QueryRequest setOffset(int offset) {
		this.offset = offset;
		return this;
	}

	public QueryRequest setSize(int size) {
		this.size = size;
		return this;
	}

	@Override
	public String toString() {
		return "QueryRequest{" +
			"queries=" + queries +
			", sortBy='" + sortBy + '\'' +
			", sortOrder='" + sortOrder + '\'' +
			", future=" + future +
			", offset=" + offset +
			", size=" + size +
			'}';
	}
}
