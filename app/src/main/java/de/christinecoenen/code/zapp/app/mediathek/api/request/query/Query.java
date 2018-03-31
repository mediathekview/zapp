package de.christinecoenen.code.zapp.app.mediathek.api.request.query;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Query implements Serializable {

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
