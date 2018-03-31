package de.christinecoenen.code.zapp.app.mediathek.api.request.query;

public class RangeQuery extends Query {

	private Range range = new Range();

	public RangeQuery setField(Field field) {
		range.setField(field);
		return this;
	}

	public RangeQuery setLt(String lt) {
		range.setLt(lt);
		return this;
	}

	public RangeQuery setLte(String lte) {
		range.setLte(lte);
		return this;
	}

	public RangeQuery setGt(String gt) {
		range.setGt(gt);
		return this;
	}

	public RangeQuery setGte(String gte) {
		range.setGte(gte);
		return this;
	}

	@Override
	public String toString() {
		return "RangeQuery{" +
			"range=" + range +
			'}';
	}
}
