package de.christinecoenen.code.zapp.app.mediathek.api.request.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Text implements Serializable {

	private List<Field> fields = new ArrayList<>();
	private String text;
	private Query.Operator operator = Query.Operator.AND;

	public void addField(Field field) {
		fields.add(field);
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "Text{" +
			"fields=" + fields +
			", text='" + text + '\'' +
			", operator=" + operator +
			'}';
	}
}
