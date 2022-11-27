package de.christinecoenen.code.zapp.models.shows

import androidx.room.Embedded
import org.joda.time.DateTime

data class SortableMediathekShow(

	var sortDate: DateTime,

	@Embedded
	var mediathekShow: MediathekShow

)
