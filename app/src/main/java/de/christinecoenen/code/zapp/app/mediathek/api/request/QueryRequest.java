package de.christinecoenen.code.zapp.app.mediathek.api.request;


import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.christinecoenen.code.zapp.app.mediathek.api.request.query.Field;
import de.christinecoenen.code.zapp.app.mediathek.api.request.query.MatchAllQuery;
import de.christinecoenen.code.zapp.app.mediathek.api.request.query.Query;
import de.christinecoenen.code.zapp.app.mediathek.api.request.query.Text;
import de.christinecoenen.code.zapp.app.mediathek.api.request.query.TextQuery;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class QueryRequest implements Serializable {

	private Query body = new MatchAllQuery();
	private List<Sort> sorts;
	private int skip = 0;
	private int limit = 10;

	public QueryRequest setSimpleSearch(String queryString) {
		if (TextUtils.isEmpty(queryString)) {
			this.body = new MatchAllQuery();
		} else {
			Text text = new Text()
				.setText(queryString)
				.addField(Field.TITLE)
				.addField(Field.TOPIC);
			this.body = new TextQuery(text);
		}
		return this;
	}

	public QueryRequest setSortAscending(Field... fields) {
		sorts = new ArrayList<>();
		for (Field field : fields) {
			sorts.add(new Sort(field, Sort.Order.DESCENDING));
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
