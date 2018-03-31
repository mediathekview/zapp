package de.christinecoenen.code.zapp.app.mediathek.api.request.query;

public class TextQuery extends Query {

	private Text text = new Text();

	public TextQuery addField(Field field) {
		this.text.addField(field);
		return this;
	}

	public TextQuery setText(String text) {
		this.text.setText(text);
		return this;
	}

	@Override
	public String toString() {
		return "TextQuery{" +
			"text=" + text +
			'}';
	}
}
