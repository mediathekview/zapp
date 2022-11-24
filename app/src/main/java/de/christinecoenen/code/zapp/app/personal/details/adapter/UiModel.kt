package de.christinecoenen.code.zapp.app.personal.details.adapter

import de.christinecoenen.code.zapp.models.shows.SortableMediathekShow
import org.joda.time.LocalDate

sealed class UiModel {

	class MediathekShowModel(sortableMediathekShow: SortableMediathekShow) : UiModel() {
		val id = sortableMediathekShow.mediathekShow.apiId
		val show = sortableMediathekShow.mediathekShow
		val date = LocalDate(sortableMediathekShow.sortDate)

		fun isOnSameDay(other: MediathekShowModel): Boolean {
			return date == other.date
		}
	}

	class DateSeparatorModel(val date: LocalDate) : UiModel()

}
