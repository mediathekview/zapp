# v-next
* System bar does not get stuck after accessing program information inside the player [#313](https://github.com/mediathekview/zapp/issues/313)
* Adjusted "About Zapp" to different device sizes

# 9.0.0
* Added option to disable search history (thanks to Bnyro)
* Added new mediathek channels ARD-alpha, One and tagesschau24
* Prepared Zapp for Android 16
* Fix subtitles of parlamentsfernsehen channel cannot be turned off ([#467](https://github.com/mediathekview/zapp/issues/467))
* Fixed bug where download buttons were shows when no download available
* Prepared Zapp for Android 15
* New seach functionality

# 9.0.0-beta1
* Fixed bug where download buttons were shows when no download available
* Prepared Zapp for Android 15
* New seach functionality

# 8.5.4
* Updated support and crash report email address

# 8.5.3
* Fixed html entities visible in program information titles
* Fixed removing a bookmark is sometimes not possible

# 8.5.2
* Removed duplicate entries when scrolling the mediathek list

# 8.5.1
* Dynamic colors were not applied on some devices

# 8.5.0
* Added settings to Android TV version
* Fixed cut off text in skip forward button
* Removed channels "Deutsche Welle" and "Deutsche Welle +", because their German version went off air

# 8.4.0
* Switched from exoplayer to new media 3 library
* Fixed crash on app start if an sd card with downloaded shows had been removed
* Prepared Zapp for Android 14
* Translated FAQ and changelog to English (thanks to eUgEntOptIc)
* Program information can be accessed via the channel list's context menus
* Updated channel logos
* Added Funk.net to mediathek
* Added SR to mediathek

# 8.3.2
* Fixed download notifications not opening the show
* Fixed wrong tab displayed after device rotation

# 8.3.1
* Fixed crash caused by changed channel order

# 8.3.0
* Settings in material design (thanks to Bnyro)
* Sleep timer (thanks to Bnyro)
* Exo player update - should fix playback problems on some devices
* Possibility to set start tab in settings (thanks to Bnyro)
* Videos use subtitle style from Android system settings

# 8.2.0
* Support for dynamic device colors
* Fixed crash in mediathek list
* Fixed dsiplay error in mediathek list on Android TV
* Switching between live streams is possible again!

# 8.1.0
* Fixed videos sometimes not loading when started from within picture in picture mode
* Video progress is saved periodically during playback
* Resume playback in background when screen gets turned off
* Preference for automatically switching to picture in picture mode

# 8.0.0
* Support for Android 13 (monochrome icon, per-app language preference)
* Language selection in app settings
* Cancelled support for Android 7
* Fixed null values in search result count
* Android TV receives up to date stream addresses
* Fixed jumping of the mediathek list during scrolling
* Overhauled download functionality
* Failed downloads will be resumed automatically
* Searchable download list
* Searchable "continue watching" list
* Searchable bookmarks
* Modern network stack for video playback
* Better error messages during video playback

# 7.0.4
* Channel list layout on tablets smoothed out
* Fixed hanging status bar in full screen mode on some devices
* Volume and brightness controls improved

# 7.0.3
* Standby mode on Android TV devices no longer activates during video playback
* Standby mode is no longer activated when returning from background playback

# 7.0.2
* Zapp made visible again for non-Android TV devices

# 7.0.1
* Player UI hides correctly automatically when coming out of background playback
* Fixed crashes on startup on older Android TV devices
* Click on download finished notification works again on Android 12

# 7.0.0 - Beta
* Official support for Android TV!
* New Material 3 design
* Support for Android backups for preferences and database entries
* Category icons are now visible in light theme
* Improved fullscreen mode for videos

# 6.1.0
* Failed downloads can be removed from download list
* Repaired video quality selection in metered networks
* Number of results is displayed inside mediathek filter

# 6.0.2
* Video player interface is hidden automatically faster
* Fixed playback errors for some mediathek shows
* Fixed deactivated download button for some mediathek shows
* Fixed app crash in mediathek list
* Fixed background playback not killed by the system after a few minutes

# 6.0.1
* Fixed crash after editing channel list
* Fixed crash when starting Tagessschau24 on Android 6
* Fixed jumping progress bar in channel list on Android 6

# 6.0.0
* extended mediathek search: filtern by channel and show duration
* removed search suggestions
* display channel logo in playback notifications
* use highest available quality when sharing mediathek shows
* add faq entry regarding chromecast support

# 5.0.2
* Fixed downloads not working

# 5.0.1
* Fixed downloads not working

# 5.0.0
* Swipen zwischen den Sendern entfernt (zur besseren Warbarkeit und Fehlerreduktion)
* Neues Layout beim Abspielen von Live-Streams
* Sprachwechsel für wenige Live-Streams verfügbar
* Untertitel in Live-Stream schnell ein- und ausschaltbar
* Seeken in Live-Streams
* Pausieren von Live-Streams
* Wiedergabegeschwindigkeit in Live-Streams anpassbar
* Fehler beim Wechsel zwischen Bild-in-Bild-Modus und Normal-Modus behoben
* Gerät darf bei pausierten Videos in den Ruhezustand gehen
* Fehlenden Ladeindikator bei Mediathekvideos wieder sichbar gemacht
* Unterstützung für Android 12

# 4.0.1
* Absturz beim Starten ohne Internetverbindung behoben

# 4.0.0
* Downloads-Tab eingeführt
* Hauptnavigation auf Tabs umgestellt
* Absturz beim Entfernen der SD-Karte behoben
* Abstürze auf älteren Android-Geräten behoben
* Mindestanforderung auf Android N angehoben
* FAQ erweitert

# 3.6.1
* Untertitel in Live-Streams abgeschaltet (können über die Android-Bedienungshilfe aktiviert werden)
* Streams von MDR repariert
* Abgeschnittene Texte in About-Screen behoben
* Verbesserungen beim Erkennen von kostenpflichtigen vs. unbeschränkten Internet-Verbindungen
* Metadaten in F-Droid repariert

# 3.6.0
* Fehler beim Download behoben
* Fehler beim laden der Untertitel behoben
* Einstellbare Wiedergabe-Geschwindigkeit

# 3.5.1
* Fehler beim Download auf SD-Karte behoben
* Fehler-Melden-Button in Download-Benachrichtigung eingebaut
* Downloads bei instabiler Internet-Verbindung verbessert

# 3.5.0
* Text- und Übersetzungs-Fehler verbessert
* Fehlertyp wird in Download-Fehler-Benachrichtigung angezeigt
* Klick auf Download-Benachrichtigung führt nicht mehr zur falschen Sendung
* Dateien abgebrochener Downloads werden gelöscht
* Abstürze bei Downloads in verschiedenen Netzwerk-Typen behoben
* Absturz behoben, wenn fehlerhafte Downloads nochmals aufgenommen werden
* MediaStore-Handling auf Android 10 verbessert

# 3.5.0 - Beta 1
* Downloads auf SD-Karte möglich
* Klick auf Download-Notificaton öffnet entsprechende Sendung
* Download-Fortschritt wird innerhalb der App angezeigt
* Vorschaubild für heruntergeladene Sendungen
* Abspiel-Position für Sendungen wird dauerhaft gespeichert
* Mediathek-Benutzeroberfläche überarbeitet
* ARTE.FR aus Mediathek entfernt
* Multiwindow-Unterstützung hinzugefügt

# 3.4.0
* Stream von ARD repariert
* Unbekannte Veröffentlichungsdaten markiert
* Falscher Download-Dateiname auf Samsung-Geräten behoben
* Fehlende Tastatur auf FireTV eingeblendet
* App-Theme auf Material-Components umgestellt
* Absturz beim Download behoben, wenn "..." im Dateinamen vorkommt

## 3.3.1
* Absturz beim Öffnen eines Live-Streams ohne Internet gefixt

## 3.3.0
* Sender können ausgeblendet werden
* Updatefehler für geänderte Stream-Urls behoben
* Stream-Urls der ZDF-Sender aktualisiert
* Letzte Suchanfragen werden als Vorschläge gespeichert
* Nicht zulässige Zeichen für manche Download-Dateien ausgeschlossen
* Nicht herunterladbare Sendungen deutlicher markiert
* Radio Bremen hinzugefügt

## 3.2.0
* Menü wird beim Zurück-Navigieren geschlossen, bevor die App beendet wird (danke an arthur-star)
* Streams von SWR repariert (danke an deejay73)
* Stream-Urls können jetzt ohne App-Update aktualisiert werden (danke an bagbag)

## 3.1.0
* Absturz beim Zurück-Navigieren behoben
* Absturz beim Zurückkehren aus dem Hintergrund-Playback behoben
* Einstellbare Streaming-Qualität im mobilen Netz
* Unterstützung für Android Q

## 3.0.0
* Zapp ist Teil von MediathekView!
* Bild-in-Bild-Funktion (Beta)
* Nachtmodus
* Downloads nur im W-Lan erlauben
* Senderlogos aktualisiert
* Feedback-Button einegfügt
* Wiedergabe kann nach Fehlern wieder aufgenommen werden
* Streams vom Parlamentsfernsehen repariert

## 2.2.2
* Absturz beim Laden der verändereten Senderliste behoben.
* Einstellungsbildschirm aktualisiert.

## 2.2.1
* Absturz beim Starten des Hintergrund-Playbacks behoben.

## 2.2.0
* Lautstärke- und Helligkeitsregler getauscht.
* Streams von ARD repariert (danke an protvis74).
* Background-Playback auf Android P repariert.
* Navigation-Bar überlappt keine UI-Elemente mehr.
* Pinch-to-Zoom auf nicht 16:9-Geräten eingebaut.
* Absturz bei Klick auf die Notification behoben.

## 2.1.1
* Diverse Abstürze behoben

## 2.1.0
* Hintergrund-Playback von Streaming-Videos.
* Changelog im Markdown-Format.
* Verbesserte Icon-Darstellung auf Samsung-Geräten.
* Zapp beendet sich sauber, wenn es im Taskmanager beendet wird.
* Downloadmöglichkeit ausgeblendet, wenn das Video nur als Stream zur Verfügung steht.
* Dem Gerät erlaubt, in den Ruhezustand zu gehen, sobald der Player nicht mehr läuft.
* Abstürze beim Teilen von Videos und Streams behoben.

## 2.0.0
* Hintergrund-Playback von Mediathek-Videos.
* Automatisches pausieren, wenn eine andere App Ton abspielt oder ein Anruf eingeht.
* Lockscreen-Widget.
* Playback startet nicht mehr automatisch, wenn das Gerät aus ist und sich mit einem WLAN wieder verbindet.
* Ladebalken wird nur noch angezeigt, wenn das Video tatsächlich stockt.
* Kleinste unterstützte Android-Version auf 5.0 Lollipop angehoben.

## 1.11.2
* Streams von DW+ repariert (danke an jw243).
* Wiedergabefehler in Android P behoben.

## 1.11.1
* Streams von ARD repariert (danke an jw243).

## 1.11.0
* Bug beim Herunterladen von Videos auf Android 8.1 behoben (danke an pmk1c).
* Absturz beim Starten ohne Internet-Verbindung behoben (danke an alle Reporter).
* Absturz beim Herunterladen von bestimmten Videos behoben.
* Lautstärke und Bildschirmhelligkeit können per Swipe geändert werden.
* Möglichkeit hinzugefügt, einen laufenden Download abzubrechen.
* User Interface überarbeitet, um Platz für neue Features zu machen.

## 1.10.0
* Einstellung zur Beschränkung auf W-Lan-Wiedergabe hinzugefügt.
* Bug beim Herunterladen von Videos behoben (danke an michaelof).
* Teilen von Videos über ein Long-Press-Menü.
* Bibliotheken und Android-Version aktualisiert.

## 1.9.1
* SSL-Fehler beim Laden der Mediathek behoben (danke an axel-rank).

## 1.9.0
* Streams von ARD One, MDR und NDR repariert.
* Absturz verhindert, wenn man auf die Download-Notification klickt.
* Adaptives App-Icon erstellt.
* Über-Sektion der App überarbeitet und verwendete Bibliotheken angezeigt.

## 1.8.1
* KiKA-Stream repariert (danke an SubhrajyotiSen).
* Korrekte Fehlermeldung angezeigt, wenn das Laden der Mediathek auf älteren Android-Versionen fehl schlägt.

## 1.8.0
* WDR-Stream repariert (danke an Medusalix).
* Download von Sendungen möglich.
* Rechtschreibfehler korrigiert (danke an kiandru und bagbag).
* ZDFneo-Logo aktualisiert (danke an bvdaakster).

## 1.7.2
* Möglichen Absturz bei Ladeproblemen behoben.

## 1.7.1
* Absturz für ältere Android-Versionen behoben.
* Verhindert, dass Mediathek-Sendungen auf Android 7 nicht geladen werden können.

## 1.7.0
* Untertitel für Mediathek-Videos.

## 1.6.0
* Rudimentäre Picture-in-Picture-Unterstützung für Android O.
* Rudimentäre Unterstützung für Android TV.
* Diverse mögliche Absturz-Ursachen behoben.
* Möglichkeit zum Senden von Absturzberichten eingebaut.
* Animationen eingebaut.

## 1.5.0
* Mediathek-Sendungen von vielen öffentlich-rechtlichen Sendern hinzugefügt. Mit freundlicher Genehmigung vom <a href="https://mediathekviewweb.de/">MediathekViewWeb-Projekt</a>.

## 1.4.2
* RBB-Streams repariert.

## 1.4.1
* Verhindere, dass der Bildschirm aus geht, während ein Stream läuft.

## 1.4.0
* Sender "Deutsche Welle Deutsch+" hinzugefügt
* Zuverlässigere Video-Wiedergabe für viele Sender.
* Verstecke Status-Bar nachdem die App pausiert wurde.

## 1.3.4
* Support für Android Kitkat und höher

## 1.3.3
* App verbraucht weniger Speicherplatz
* Programminformationen werden von einem einzigen Backend geladen
* Zapp ist jetzt auch in Google Play zu finden

## 1.3.2
* Tastaturunterstützung wieder hergestellt

## 1.3.1
* Die Fortschrittsanzeige ist jetzt auch für Arte-Sendungen verfügbar
* Sender der ZDF-Gruppe zeigen wieder Programminformationen an

## 1.3.0
* Alle Einstellungen auf einem Bildschirm gesammelt
* Einstellung für das Sperren der Bildschirmrotation hinzugefügt
* FAQ für allgemeine Informationen über Zapp eingeführt
* App-Shortcuts für Android 7.1 eingeführt

## 1.2.1
* Layout von "Sender anordnen"-Bildschirm verbessert
* Programminformation werden nicht mehr herunter geladen, wenn die App pausiert wurde
* Möglichen Absturz auf machen Geräten oder Emulatoren behoben, wenn man einen Kanal auswählt
* Parlamentsfernsehen-Streams auf http umgestellt, damit sie wieder abspielen

## 1.2.0
* Senderreihenfolge kann beliebig geändert werden
* Portrait-Modus ist jetzt auch für die Streams erlaubt
* Eingestellte Zeitzonen werden nicht mehr in den Programminformationen ignoriert
* Unterstützung für Multiwindow-Modus
* Unterstützung für Android Nougat
* In Fehlermeldungen auf mögliches Geoblocking hingewiesen

## 1.1.0
* Die Farbe des UIs ändert sich mit dem ausgewählten Sender
* Bessere Fehlermeldungen für den Video-Player
* Anfragen für Programminformationen werden nicht mehr doppelt abgeschickt
* Unter regionalen Sender-Logos wird die Region angezeigt
* Sender-Logos werden zentriert und Logos mit Verläufen richtig angezeigt
* Unterstützung von Tastaturen und Amazon-Fire-TV-Fernbedienung
* ARD-Programminfo wurde beim Start nicht geladen

## 1.0.1
* Mögliche Endlosschleife im ARD Programminfo-Downloader entfernt

## 1.0.0
* ZDF.kultur entfernt, weil der Sender eingestellt wird
* Programminformationen für laufende Sendungen werden angezeigt
* Changelog-Überschriften auf deutsch übersetzt

## 0.2.0
* Changelog eingefügt
* Rahmen um das Phoenix-Logo entfernt
* Sender-Logo wird nach dem Pausieren der App angezeigt, bis der Stream wieder geladen ist
* Sender-Sortierung verbessert: deutschlandweite Sender stehen jetzt weiter oben in der Liste
* Streams fangen nicht sofort an zu laden, wenn man schnell durch die Sender schaltet
* Sender-Informationen in JSON-Datei gespeichert
