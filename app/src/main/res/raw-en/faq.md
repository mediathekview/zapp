## The videos are not playing - what should I do?

If the video streams are not playable on Android, it can have several reasons:

- Maybe you're using Zapp outside of Germany. Some broadcasters block access to their streams or individual shows for users outside Germany. If you are not sure, go to the corresponding broadcaster's website on your PC and see if the live stream is running there. If you get an error message about **geoblocking**, Zapp can't do anything about it at the moment.

- Maybe your internet connection is slow, unreliable or blocked by a firewall.

- Maybe your device doesn't support the video codec. You can start streaming in another video player by pressing the menu button after selecting a channel. Click "Share" and open the stream with Google Video Player. If Google Video Player can't play the stream either, the video format is almost certainly not supported by your device.


## Why don't I see program information for some stations?

Most broadcasters do not have a public API to retrieve program information. Because of this, Zapp tries to search the broadcasters' websites for the shows that are currently running. This approach is not very reliable and can quickly break if a web page is changed or updated.


## Why are some channels like RTL or Pro7 missing?

Zapp only streams public broadcasters. Channels like RTL and Pro7 belong to private companies and may not be streamed without payment.


## Why are shows missing from the Mediathek (media library)?

Zapp is part of [MediathekView](https://mediathekview.de/) and therefore uses the Mediathek API of [MediathekViewWeb](https://mediathekviewweb.de/). However, most broadcasters do not have a public API to query broadcast information. Therefore it can happen that channels or individual programs are missing or program information is incomplete.


## Why are programs shown twice in the Mediathek (media library)?

It often happens that the same program is uploaded in the media libraries of different broadcasters. Since Zapp displays all available shows, it sometimes looks like shows are displayed twice.


## Why are subtitles not displayed even though I have them enabled?

Sometimes subtitles are delivered by media libraries in an unexpected format, so Zapp can't display them. Unfortunately, there is no way to detect these errors beforehand. However, it is often enough to wait a bit for the subtitles to finish loading.


## Can Zapp show the subtitles of the live streams?

Some broadcasters like ZDF offer subtitles in some programs of the live streams. Zapp shows the subtitles automatically if they have been activated system-wide in the Android settings. Here you can also customize the appearance of the subtitles on some systems.


## Why does a channel always show the same video snippet?

The channel is no longer available at the address Zapp expects and needs to be updated. Please contact the developers so that the problem can be solved.


## Why is the video image cropped or has black borders left and right?

Your display has another aspect ratio than the video. You can use the pinch gesture (pinch two fingers together) to display the video smaller or larger.


## Why doesn't Zapp support Chromecast?

Chromecast can only be included in Android apps via a non-open source library. Since these libraries are not allowed to be distributed via F-Droid, Zapp cannot support Chromecast.
