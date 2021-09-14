package de.christinecoenen.code.zapp.app.mediathek.api.result

import androidx.annotation.Keep

@Keep
data class MediathekAnswer(

	val err: String? = null,
	val result: MediathekAnswerResult? = null

)
