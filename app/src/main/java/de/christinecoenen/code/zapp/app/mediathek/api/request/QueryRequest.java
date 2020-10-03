package de.christinecoenen.code.zapp.app.mediathek.api.request;


import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedReturnValue"})
public class QueryRequest implements Serializable {

	@SuppressWarnings("FieldCanBeLocal")
	private final String[] ALLOWED_CHANNELS = new String[]{
		"ARD",
		"ZDF",
		"SWR",
		"NDR",
		"BR",
		"ARTE.DE",
		"DW",
		"3Sat",
		"SRF",
		"MDR",
		"SR",
		"KiKA",
		"HR",
		"RBB",
		"ORF",
		"WDR",
		"rbtv",
		"PHOENIX",
		"ZDF-tivi",
		"SRF.Podcast"
	};

	private final List<Query> queries = new ArrayList<>();
	private final List<Query> alowedChannelsQueries = new ArrayList<>();

	private String sortBy = "timestamp";
	private String sortOrder = "desc";

	@SuppressWarnings("FieldCanBeLocal")
	private final boolean future = false;
	private int offset = 0;
	private int size = 30;

	public QueryRequest() {
		for (String allowedChannel : ALLOWED_CHANNELS) {
			this.alowedChannelsQueries.add(new Query(allowedChannel, "channel"));
		}

		ResetQueries();
	}

	public QueryRequest setSimpleSearch(String queryString) {
		ResetQueries();

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

	private void ResetQueries() {
		this.queries.clear();
		this.queries.addAll(alowedChannelsQueries);
	}

	@NonNull
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
