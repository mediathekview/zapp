package de.christinecoenen.code.zapp.app.mediathek.api.request

import android.text.TextUtils
import androidx.annotation.Keep
import java.io.Serializable

@Keep
class QueryRequest : Serializable {

	companion object {
		private val AllowedChannels = MediathekChannel.values().map { channel -> channel.apiId }
	}

	@Suppress("unused")
	private val sortBy: String = "timestamp"

	@Suppress("unused")
	private val sortOrder: String = "desc"

	@Suppress("unused")
	private val future: Boolean = false

	var offset: Int = 0
	var size: Int = 30

	private val queries: MutableList<Query> = mutableListOf()

	@Transient
	private val alowedChannelsQueries: MutableList<Query> = mutableListOf()

	init {
		for (allowedChannel in AllowedChannels) {
			alowedChannelsQueries.add(Query(allowedChannel, "channel"))
		}

		resetQueries()
	}

	fun setSimpleSearch(queryString: String?): QueryRequest {
		resetQueries()

		if (!TextUtils.isEmpty(queryString)) {
			queries.add(Query(queryString!!, "title", "topic"))
		}

		return this
	}

	private fun resetQueries() {
		queries.clear()
		queries.addAll(alowedChannelsQueries)
	}
}
