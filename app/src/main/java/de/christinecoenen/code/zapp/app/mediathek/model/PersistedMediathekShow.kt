package de.christinecoenen.code.zapp.app.mediathek.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class PersistedMediathekShow {

	@PrimaryKey
	var id = 0

	var downloadId = 0

	var downloadedVideoUri: String? = null

	var downloadStatus = 0

	@Embedded
	var mediathekShow: MediathekShow? = null

}
