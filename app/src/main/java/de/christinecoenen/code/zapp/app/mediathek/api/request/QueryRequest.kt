package de.christinecoenen.code.zapp.app.mediathek.api.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
class QueryRequest : Serializable {

	private val sortBy: String = "timestamp"
	private val sortOrder: String = "desc"

	var future: Boolean = true
	var offset: Int = 0
	var size: Int = 30

	@SerializedName("duration_min")
	var minDurationSeconds: Int = 0

	@SerializedName("duration_max")
	var maxDurationSeconds: Int? = null

	private val queries: MutableList<Query> = mutableListOf()

	@Transient
	private val channels: MutableSet<MediathekChannel> = mutableSetOf()

	@Transient
	private var queryStrings: MutableSet<String> = mutableSetOf()

	init {
		resetQueries()
	}

	fun setChannels(channels: List<MediathekChannel>): QueryRequest = apply {
		this.channels.clear()
		this.channels.addAll(channels)

		resetQueries()
	}

	fun setQueryString(queryString: String): QueryRequest = apply {
		this.queryStrings.clear()
		this.queryStrings.add(queryString)

		resetQueries()
	}

	fun setQueryStrings(queryStrings: List<String>): QueryRequest = apply {
		this.queryStrings.clear()
		this.queryStrings.addAll(queryStrings)

		resetQueries()
	}

	private fun resetQueries() {
		queries.clear()

		// set search query
		queries.addAll(queryStrings.filter { it.isNotEmpty() }.map { queryString ->
			Query(queryString, "title", "topic")
		})

		// We do not allow an empty channel Filter as the result would always be empty.
		// Instead we filter for all available channels. This also excludes all channels
		// not defined in MediathekChannel enum (like ARTE.FR).
		//Timber.d(channels.size.toString())
		if (channels.isEmpty()) {
			channels.addAll(MediathekChannel.entries.toTypedArray())
		}

		// set all currently allowed channels
		for (allowedChannel in channels) {
			queries.add(Query(allowedChannel.apiId, "channel"))
		}
	}

	fun isEmpty() = queries.isEmpty()

	override fun toString(): String {
		return "QueryRequest(sortBy='$sortBy', sortOrder='$sortOrder', future=$future, offset=$offset, size=$size, queries=$queries)"
	}
}
