package de.christinecoenen.code.zapp.models.search

class DurationQuerySet(
	vararg queries: DurationQuery
) {

	private var _queries = mutableSetOf<DurationQuery>()

	val minDurationSeconds
		get() = _queries.find { it.comparison == Comparison.GreaterThan }?.let { it.minutes * 60 }

	val maxDurationSeconds
		get() = _queries.find { it.comparison == Comparison.LesserThan }?.let { it.minutes * 60 }

	init {
		this._queries.addAll(queries)
	}

	fun <R> map(transform: (DurationQuery) -> R): List<R> = _queries.map(transform)

	fun addOrReplace(durationQuery: DurationQuery): DurationQuerySet {
		val queries = _queries
			.filterNot {
				it.comparison == durationQuery.comparison ||
					it.comparison == Comparison.LesserThan && it.minutes < durationQuery.minutes ||
					it.comparison == Comparison.GreaterThan && it.minutes > durationQuery.minutes
			}
			.plus(durationQuery)
			.sortedBy { it.comparison }
			.toTypedArray()

		return DurationQuerySet(*queries)
	}

	fun remove(durationQuery: DurationQuery): DurationQuerySet {
		val queries = _queries
			.minus(durationQuery)
			.toTypedArray()

		return DurationQuerySet(*queries)
	}
}
