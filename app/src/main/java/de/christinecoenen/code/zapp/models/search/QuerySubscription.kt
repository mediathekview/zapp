package de.christinecoenen.code.zapp.models.search

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class QuerySubscription(
	@PrimaryKey val query: String
)
