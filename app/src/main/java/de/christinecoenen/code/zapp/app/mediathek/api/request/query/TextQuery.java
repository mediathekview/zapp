package de.christinecoenen.code.zapp.app.mediathek.api.request.query;

public class TextQuery extends Query {

	private Text text;

	public TextQuery() {
	}

	public TextQuery(Text text) {
		this.text = text;
	}

	public TextQuery setText(Text text) {
		this.text = text;
		return this;
	}

	@Override
	public String toString() {
		return "TextQuery{" +
			"text=" + text +
			'}';
	}
}
