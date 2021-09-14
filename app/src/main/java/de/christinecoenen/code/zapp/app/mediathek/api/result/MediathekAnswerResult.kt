package de.christinecoenen.code.zapp.app.mediathek.api.result

import androidx.annotation.Keep
import de.christinecoenen.code.zapp.models.shows.MediathekShow

@Keep
data class MediathekAnswerResult(

	var results: List<MediathekShow>

)
