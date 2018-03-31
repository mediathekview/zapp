package de.christinecoenen.code.zapp.app.mediathek.api.request.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Text implements Serializable {

	private List<Field> fields = new ArrayList<>();
	private String text;
	private Query.Operator operator = Query.Operator.AND;

	public Text addField(Field field) {
		fields.add(field);
		return this;
	}

	public Text setText(String text) {
		this.text = text;
		return this;
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
