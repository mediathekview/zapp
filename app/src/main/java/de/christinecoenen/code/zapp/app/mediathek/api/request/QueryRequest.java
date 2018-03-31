package de.christinecoenen.code.zapp.app.mediathek.api.request;


import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.christinecoenen.code.zapp.app.mediathek.api.request.query.BoolQuery;
import de.christinecoenen.code.zapp.app.mediathek.api.request.query.Field;
import de.christinecoenen.code.zapp.app.mediathek.api.request.query.MatchAllQuery;
import de.christinecoenen.code.zapp.app.mediathek.api.request.query.RangeQuery;
import de.christinecoenen.code.zapp.app.mediathek.api.request.query.TextQuery;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class QueryRequest implements Serializable {

	private static final TextQuery EXCLUDE_CHANNELS_QUERY = new TextQuery()
		.addField(Field.CHANNEL)
		.setText("ARTE.FR");
	private static final RangeQuery EXCLUDE_FUTURE_QUERY = new RangeQuery()
		.setField(Field.TIMESTAMP)
		.setLte("now+2h/m");

	private transient MatchAllQuery noSearchQuery = new MatchAllQuery();
	private transient TextQuery searchQuery = new TextQuery()
		.addField(Field.TITLE)
		.addField(Field.TOPIC);

	private BoolQuery body = new BoolQuery()
		.setNotQueries(EXCLUDE_CHANNELS_QUERY)
		.setMustQueries(noSearchQuery)
		.setFilterQueries(EXCLUDE_FUTURE_QUERY);

	private List<Sort> sort;
	private int skip = 0;
	private int limit = 10;

	public QueryRequest setSimpleSearch(String queryString) {
		if (TextUtils.isEmpty(queryString)) {
			body.setMustQueries(noSearchQuery);
		} else {
			searchQuery.setText(queryString);
			body.setMustQueries(searchQuery);
		}
		return this;
	}

	public QueryRequest setSortAscending(Field... fields) {
		sort = new ArrayList<>();
		for (Field field : fields) {
			sort.add(new Sort(field, Sort.Order.DESCENDING));
		}
		return this;
	}

	public QueryRequest setOffset(int offset) {
		this.skip = offset;
		return this;
	}

	public QueryRequest setSize(int size) {
		this.limit = size;
		return this;
	}
}
