var exec = require("cordova/exec");

var VideoPlayback = {
    play: function(url) {
        exec(null, null, "VideoPlayback", "playVideo", [url]);
    }
};


module.exports = VideoPlayback;
