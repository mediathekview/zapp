package de.christinecoenen.code.zapp.app.mediathek.api.request

import java.io.Serializable

internal data class Query(val query: String, val fields: List<String>) : Serializable {

	constructor(query: String, vararg fields: String) : this(query, fields.toList())

}
