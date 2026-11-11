package de.christinecoenen.code.zapp.models.collections

import androidx.room.Entity

/**
 * Associates a show (by its mediathek api id) with a collection.
 *
 * The entries are rebuilt whenever the mediathek has been searched for the collection.
 * They are used to tell which of the locally stored shows really belong to the
 * collection, e.g. to count the shows that have not been watched yet.
 */
@Entity(primaryKeys = ["collectionId", "apiId"])
data class ShowCollectionEntry(
	val collectionId: Int,
	val apiId: String
)
