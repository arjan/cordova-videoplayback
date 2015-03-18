var exec = require("cordova/exec");

var VideoPlayback = {
  play: function(url) {
    exec(null, null, "VideoPlayback", "playVideo", [url]);
  },
  ensureDownloaded: function(url, onSuccess, onError) {
    exec(onSuccess, onError, "VideoPlayback", "ensureDownloaded", [url]);
  }
};

module.exports = VideoPlayback;
