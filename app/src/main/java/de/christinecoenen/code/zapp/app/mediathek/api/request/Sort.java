package de.christinecoenen.code.zapp.app.mediathek.api.request;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import de.christinecoenen.code.zapp.app.mediathek.api.request.query.Field;

public class Sort implements Serializable {

	public enum Order {
		@SerializedName("ascending")
		ASCENTING,

		@SerializedName("descending")
		DESCENDING
	}

	private Field field;
	private Order order;

	public Sort() {
	}

	Sort(Field field, Order order) {
		this.field = field;
		this.order = order;
	}

	@Override
	public String toString() {
		return "Sort{" +
			"field='" + field + '\'' +
			", order=" + order +
			'}';
	}
}
