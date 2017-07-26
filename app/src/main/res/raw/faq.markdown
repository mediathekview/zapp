### Die Videos laufen nicht – was soll ich tun?

Wenn die Video-Streams auf Android nicht abspielen kann das mehrere Gründe haben:

- Vielleicht benutzt du Zapp außerhalb Deutschlands. Einige Sender blockieren
den Zugriff auf ihre Streams oder einzelne Sendungen für Nutzer außerhalb
Deutschlands. Wenn du dir nicht sicher bist, rufe die entsprechende Sender-Webseite
auf deinem PC auf und schaue, ob der Live-Stream dort läuft. Wenn du dort eine
Fehlermeldung über **Geoblocking** bekommst, kann Zapp daran im Moment leider nichts
ändern.

- Vielleicht ist deine Internet-Verbindung langsam, unzuverlässig oder wird von
einer Firewall blockiert.

- Vielleicht unterstützt dein Gerät den Video-Codec nicht. Du kannst den Stream in
einem anderen Videoplayer starten, indem du den Menü-Button drückst, nachdem du einen
Sender ausgewählt hast. Klicke auf "Teilen" und öffne den Stream mit Google Video Player.
Wenn Google Video Player den Stream auch nicht abspielen kann, wird das Videoformat
ziemlich sicher nicht von deinem Gerät unterstützt.


### Warum sehe für einige Sender keine Programminformationen?

Die meisten Sender haben keine öffentliche API, um Sendungsinformationen abfragen zu
können. Deswegen versucht Zapp die Webseiten der Sender nach den gerade laufenden
Sendungen zu durchsuchen. Dieser Ansatz ist nicht besonders zuverlässig und kann schnell
kaputt gehen, wenn eine Webseite geändert oder aktualisiert wird.


### Warum fehlen einige Sender wie RTL oder Pro7?

Zapp streamt nur öffentlich-rechtliche Sender. Sender wie RTL und Pro7 gehören privaten
Firmen und dürfen ohne Bezahlung nicht gestreamt werden.


### Warum fehlen Sendungen in der Mediathek?

Zapp darf freundlicherweise die Mediathek-API von [MediathekWebView](https://mediathekviewweb.de/)
benutzen. Die meisten Sender haben allerdings keine öffentliche API, um Sendungsinformationen
abfragen zu können. Daher kann es passieren, dass Sender oder einzelne Sendungen fehlen oder
Sendungsinformationen auch einmal unvollständig sind.


### Warum werden in der Mediathek Sendungen doppelt angezeigt?

Es kommt öfter vor, dass die gleiche Sendung in den Mediatheken verschiedener Sender
hochgeladen wird. Da Zapp alle verfügbaren Sendungen anzeigt sieht es dann manchmal so aus,
als würden Sendungen doppelt angezeigt.
