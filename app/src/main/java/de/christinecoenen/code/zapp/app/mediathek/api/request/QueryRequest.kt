package de.christinecoenen.code.zapp.app.mediathek.api.request

import android.text.TextUtils
import androidx.annotation.Keep
import java.io.Serializable

@Keep
class QueryRequest : Serializable {

	@Suppress("unused")
	private val sortBy: String = "timestamp"

	@Suppress("unused")
	private val sortOrder: String = "desc"

	@Suppress("unused")
	private val future: Boolean = true

	var offset: Int = 0
	var size: Int = 30

	private val queries: MutableList<Query> = mutableListOf()

	@Transient
	private val channels = MediathekChannel.values().toMutableSet()

	@Transient
	private var queryString: String? = null

	init {
		resetQueries()
	}

	fun setChannel(channel: MediathekChannel, enabled: Boolean): QueryRequest {
		if (enabled) {
			channels.add(channel)
		} else {
			channels.remove(channel)
		}

		resetQueries()

		return this
	}

	fun setQueryString(queryString: String?): QueryRequest {
		this.queryString = queryString

		resetQueries()

		return this
	}

	private fun resetQueries() {
		queries.clear()

		// set search query
		if (!TextUtils.isEmpty(this.queryString)) {
			queries.add(Query(this.queryString!!, "title", "topic"))
		}

		// set all currently allowed channels
		for (allowedChannel in channels) {
			queries.add(Query(allowedChannel.apiId, "channel"))
		}
	}

	override fun toString(): String {
		return "QueryRequest(sortBy='$sortBy', sortOrder='$sortOrder', future=$future, offset=$offset, size=$size, queries=$queries)"
	}
}
