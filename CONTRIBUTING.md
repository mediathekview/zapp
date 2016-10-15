# How to contribute

## How to add / change / fix code

1. If you plan to make a significant change to the codebase, please create an 
issue first. This way we can discuss if and how and for which version your issue 
can be resolved best. For minor changes like correcting typos you don't need to 
create an issue first. Don't be afraid to ask if you have any questions about 
contributing to Zapp.

2. [Fork](https://help.github.com/articles/fork-a-repo/) the Zapp repository on 
GitHub, because you will have no write access on the original repository.

3. Create a branch you will use for the feature or fix. Read [a couple of tips](https://stackoverflow.com/questions/14680711/how-to-do-a-github-pull-request#14681796) 
on how to do pull requests.

4. Do the coding for your feature or fix. Use [proper commit messages](http://chris.beams.io/posts/git-commit/). Follow [this styleguide](https://github.com/ribot/android-guidelines/blob/master/project_and_code_guidelines.md) 
when in doubt (except prefixing member variables and using spaces for indentation).

5. Test your changes on an actual device or an emulator. If you added a channel 
run the `JsonChannelListTest`.

6. Fix any lint errors. Run `Analyze > Inspect Code > Whole project` and make 
sure no errors or warnigs show up. Only suppress warnings if you know what you 
are doing.

7. [Create a pull request](https://help.github.com/articles/creating-a-pull-request/) 
on GitHub.


## How to build

Zapp is an Android project build with Android Studio. To build it on your machine 
make sure to install the most current version of [Android Studio](https://developer.android.com/studio/index.html) bundled with the Android SDK.

Once installed you can check out the development version of Zapp clicking
`File > New > Project from Version Control > GitHub`. Enter your credentials.
Now you can enter the Zapp repository url of your fork (https://github.com/[yourname]/zapp) 
and the directory you want the project to be saved to.

Now you should be ablte to build Zapp and run it on your device or do any other 
Android Studio operation.


## How to add a new channel

 1. Edit [channels.json](app/src/main/res/raw/channels.json) to contain your channel. The rules
 are:
  - `id` must be lowercase letters (no umlauts), numbers and underscore only
  - `id` has to be unique
  - a long `name` may contain soft hyphens to improve word breaks
  - `stream_url` has to be the full url to the m3u8 manifest (f4m won't play)
  - `logo_name` has to start with `channel_logo_`
  - only define `subtitle` if there are multiple channels with the same name/logo
  - `color` has to be the darkest of the vibrant logo colors
2. Run `JsonChannelListTest` and make sure all tests pass.
3. Test the changes on an actual device (emulators may have trouble with video 
playback).


## How to add a programmguide plugin

If you added a new channel, you can write a programmguide plugin to get metadata 
of the show currently running on this channel.

1. Add the new channel to [Channel.java](programguide/src/main/java/de/christinecoenen/code/programguide/model/Channel.java). 
Make sure the id maches the one provided inside `channels.json`.
2. Add a new Downloader (extending `BaseProgramGuideDownloader`) and a new Parser 
for the API. Use the [Jsoup API](https://jsoup.org/) for parsing and [Volley](https://developer.android.com/training/volley/index.html). 
Use the existing Downloaders as reference. Make sure to respect the devices 
configured timezone.
3. Add your downloader to the [PluginRegistry](programguide/src/main/java/de/christinecoenen/code/programguide/plugins/PluginRegistry.java).
4. Test a few days on your device.
