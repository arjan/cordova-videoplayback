Native video playback cordova plugin
====================================

This plugin plays videos which are hosted on external URL in your
app. It does so by first (one-time) downloading the video to the
device before starting the playback.

Installation
------------

    cordova plugin add https://github.com/arjan/cordova-videoplayback


Usage
-----

    window.VideoPlayback.play("http://example.com/videos/test.mp4");


Requirements / limitations
--------------------------

 * Tested with Cordova 3.5.0
 * Android only currently (iOS support is planned)
 * MP4 files must be playable by the native Android MediaPlayer. (it must be encoded with the x264 baseline profile)

License
-------

This plugin is copyright 2015, Arjan Scherpenisse. Licensed under the
MIT license.

