package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import de.christinecoenen.code.zapp.models.shows.MediathekShow
import org.joda.time.DateTime
import org.joda.time.LocalDate

sealed class UiModel {

	class MediathekShowModel(
		val show: MediathekShow,
		date: DateTime
	) : UiModel() {

		val localDate = LocalDate(date)

		fun isOnSameDay(other: MediathekShowModel): Boolean {
			return localDate == other.localDate
		}

	}

	class DateSeparatorModel(val date: LocalDate) : UiModel()

}
