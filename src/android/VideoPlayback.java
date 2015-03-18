/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 */

package nl.miraclethings.videoplayback;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class VideoPlayback extends CordovaPlugin {

    private VideoPlaybackDispatcher dispatcher;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        dispatcher = new VideoPlaybackDispatcher(cordova.getActivity());
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        try {
            if (action.equals("playVideo")) {
                dispatcher.playVideo(args.getString(0));
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ""));
            }

            else if (action.equals("ensureDownloaded")) {
                dispatcher.ensureDownloaded(args.getString(0), new VideoPlaybackDispatcher.VideoDownloadCallback() {
                    @Override
                    public void onDownloadResult(String url, Exception error) {
                        if (error == null) {
                            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ""));
                        } else {
                            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
                        }
                    }
                });
            }

            else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, ""));
            }

        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
        }
        return true;
    }

}
