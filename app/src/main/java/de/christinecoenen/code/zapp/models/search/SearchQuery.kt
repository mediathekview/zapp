package de.christinecoenen.code.zapp.models.search

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity
data class SearchQuery(

	@PrimaryKey
	val query: String,

	val date: DateTime
	
)
