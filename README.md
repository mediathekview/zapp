# ZAPP

Zapp gives you easy access to a large number of German public broadcasting live streams. Watch ARD, ZDF and other channels live. Zapp makes it easy to quickly switch between channels.

![ZAPP logo](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

**Please note:** If you use Zapp outside Germany some channels – like ZDF – may be geoblocked.

---------------------

## FAQ

### The videos do not play, what should I do?

The are multiple reasons why the streams won't play on Android:

- Maybe you are using Zapp outside of Germany. Some channels may block access
to their streams or to single shows for users outside of Germany. When in doubt
head the the channels website (using a desktop) and check out the live stream
there. If you get an error message about **geoblocking** there, Zapp can't do 
anything about it right now.

- Maybe your internet connection is wonky or blocked by a firewall.

- Maybe you Android device can't handle the video codec. You can share the 
stream to another video player by pressing the menu button after selecting a
channel. Click "Teilen" and let Google Video Player open the stream. If Google
Video Player is not able to play back the stream your device most likely cannot
handle the video format.


## Why don't I see any show info for a specific channel?

Most channels don't have any public API to get EPG data from. So Zapp tries to
parse channel websites for the shows currently running. This approach is not
very reliable and may break whenever the websites change or update.


## Why don't I see the shows progress on ARTE?

ARTEs otherwise excellent API does not return the shows start and end time or
progress so Zapp can't display it.


### Why are there channels missing, like RTL or Pro7?

Zapp does only stream public broadcasting channels. Channels like RTL and Pro7
are owned by private companies and are not allowed to be streamed without paying
for them.
