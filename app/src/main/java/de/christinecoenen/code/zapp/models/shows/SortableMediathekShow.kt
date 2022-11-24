package de.christinecoenen.code.zapp.models.shows

import androidx.room.Embedded
import org.joda.time.DateTime

data class SortableMediathekShow(

	var sortDate: DateTime? = null,

	@Embedded
	var mediathekShow: MediathekShow

)
