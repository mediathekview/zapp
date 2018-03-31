package de.christinecoenen.code.zapp.app.mediathek.api.request.query;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public abstract class Query implements Serializable {

	@SuppressWarnings("unused")
	public enum Operator {
		@SerializedName("and")
		AND,

		@SerializedName("or")
		OR
	}

	@Override
	public String toString() {
		return "Query{}";
	}

}
