package de.christinecoenen.code.zapp.app.livestream.api.model

class ShowResponse {

	private val shows: List<Show> = listOf()

	val show: Show
		get() = shows[0]

	val isSuccess: Boolean
		get() = !shows.isEmpty()
}
