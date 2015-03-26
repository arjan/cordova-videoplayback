var exec = require("cordova/exec");

var VideoPlayback = {
  play: function(url) {
    exec(function(){}, function(){}, 'VideoPlayback', 'playVideo', [url, 'NO']);
  },
  ensureDownloaded: function(url, onSuccess, onError) {
    exec(onSuccess, onError, "VideoPlayback", "ensureDownloaded", [url]);
  }
};

module.exports = VideoPlayback;
