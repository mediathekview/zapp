package de.christinecoenen.code.zapp.app.mediathek

import de.christinecoenen.code.zapp.models.shows.MediathekShow

object MediathekData {

	val testShow: MediathekShow
		get() = MediathekShow(
			apiId = "test123",
			channel = "ARD",
			title = "My Title",
			videoUrl = "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"
		)

}
