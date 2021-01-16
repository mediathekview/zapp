package de.christinecoenen.code.zapp.app.livestream.api.model

class ShowResponse {

	private val shows: List<Show>? = null

	val show: Show
		get() = shows!![0]

	val isSuccess: Boolean
		get() = shows != null && shows.isNotEmpty()
}
