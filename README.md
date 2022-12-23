[![Build and test](https://github.com/mediathekview/zapp/actions/workflows/build.yml/badge.svg)](https://github.com/mediathekview/zapp/actions/workflows/build.yml)

<a href="https://f-droid.org/repository/browse/?fdid=de.christinecoenen.code.zapp" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80"/></a>

![ZAPP](fastlane/metadata/android/en-US/images/featureGraphic.jpg)

1. [Über](#über)
2. [FAQ](app/src/main/res/raw/faq.md)
3. [Changelog](app/src/main/res/raw/changelog.md)
4. [Bibliotheken](#bibliotheken)

---------------------

## Über

Zapp ist Teil von MediathekView und gibt dir einen schnellen Zugang zu vielen deutschen öffentlich-rechtlichen Fernsehsendern. Schaue ARD, ZDF und andere Sender live oder aus der Mediathek.

<b>Features:</b>
<ul>
	<li>Android-Client für MediathekView</li>
	<li>Live-Streams von über 30 Sendern</li>
	<li>Adaptives Streaming — passend zur Geschwindigkeit deiner Netzwerkverbindung</li>
	<li>Schneller Sender-Wechsel</li>
	<li>Aktuelle Programminformationen</li>
	<li>Sender-Shortcuts für neuere Android-Versionen</li>
	<li>Sendungen aus fast 20 Mediatheken zum Streamen</li>
	<li>Bequeme Sendungssuche in der Mediathek</li>
	<li>Zuschaltbare Untertitel für einige Mediathek-Sendungen</li>
	<li>Teilen-Funktion für Live- und Mediathek-Videos</li>
	<li>Hintergrund-Playback</li>
</ul>

**Achtung:** Wenn du Zapp außerhalb Deutschlands benutzt, können manche Sender — wie zum Beispiel ZDF — geblockt sein.

<img src="fastlane/metadata/android/de/images/phoneScreenshots/01.jpg" width="24%" alt="video screen" /> <img src="fastlane/metadata/android/de/images/phoneScreenshots/02.jpg" width="24%" alt="video screen" /> <img src="fastlane/metadata/android/de/images/phoneScreenshots/03.jpg" width="24%" alt="video screen" /> <img src="fastlane/metadata/android/de/images/phoneScreenshots/04.jpg" width="24%" alt="video screen" /><img src="fastlane/metadata/android/de/images/phoneScreenshots/05.jpg" width="49%" alt="video screen" /> <img src="fastlane/metadata/android/de/images/phoneScreenshots/06.jpg" width="49%" alt="video screen" />

---------------------

## Übersetzungen

Zapp wird in Deutsch entwickelt, kann aber über [Hosted Weblate](https://hosted.weblate.org/projects/zapp/) in andere Sprachen übersetzt werden. 
<a href="https://hosted.weblate.org/engage/zapp/">
<img src="https://hosted.weblate.org/widgets/zapp/-/horizontal-auto.svg" alt="Status" />
</a>

## Bibliotheken

Zapp uses a lot of awesome libre-software libraries:
- [JUnit](https://junit.org/junit4/) to test things out
- [Gson](https://github.com/google/gson) to parse the list of channels
- [Commons IO](https://commons.apache.org/proper/commons-io/) for a sane way to handle files on Android
- [DragListView](https://github.com/woxblom/DragListView) to let you reorder the channel list intuitively
- [ExoPlayer](https://google.github.io/ExoPlayer/) the better video player
- [Retrofit](https://square.github.io/retrofit/) for easy server api communication
- [Joda-Time](https://www.joda.org/joda-time/) for sane date and time handling in Java
