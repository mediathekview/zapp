package de.christinecoenen.code.zapp.app.mediathek;

import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;

public class MediathekData {

	public static MediathekShow getTestShow() {
		MediathekShow mediathekShow = new MediathekShow();
		mediathekShow.setApiId("test123");
		mediathekShow.setChannel("ARD");
		mediathekShow.setTitle("My Title");
		mediathekShow.setVideoUrl("http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4");
		return mediathekShow;
	}

}
