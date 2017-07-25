package de.christinecoenen.code.zapp.app.mediathek.api.request;


import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class QueryRequest implements Serializable {

	private final List<Query> queries = new ArrayList<>();

	private String sortBy = "timestamp";
	private String sortOrder = "desc";

	@SuppressWarnings("FieldCanBeLocal")
	private final boolean future = false;
	private int offset = 0;
	private int size = 10;

	public QueryRequest setSimpleSearch(String queryString) {
		this.queries.clear();
		if (!TextUtils.isEmpty(queryString)) {
			this.queries.add(new Query(queryString, "title", "topic"));
		}
		return this;
	}

	public QueryRequest addQuery(String fieldName, String queryString) {
		this.queries.add(new Query(queryString, fieldName));
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
