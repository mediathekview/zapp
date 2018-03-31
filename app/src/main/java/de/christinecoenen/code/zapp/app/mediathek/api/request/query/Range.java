package de.christinecoenen.code.zapp.app.mediathek.api.request.query;

public class Range {

	private Field field;
	private String lt;
	private String lte;
	private String gt;
	private String gte;

	public void setField(Field field) {
		this.field = field;
	}

	public void setLt(String lt) {
		this.lt = lt;
	}

	public void setLte(String lte) {
		this.lte = lte;
	}

	public void setGt(String gt) {
		this.gt = gt;
	}

	public void setGte(String gte) {
		this.gte = gte;
	}

	@Override
	public String toString() {
		return "Range{" +
			"field=" + field +
			", lt='" + lt + '\'' +
			", lte='" + lte + '\'' +
			", gt='" + gt + '\'' +
			", gte='" + gte + '\'' +
			'}';
	}
}
