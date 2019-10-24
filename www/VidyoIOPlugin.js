var exec = require('cordova/exec');

var jsCallback;

function VidyoIOPlugin() {}

/**
 * Handle callback from native side
 * 
 * @param {JSON} response 
 */
var nativeResponseCallback = function(response) {
    console.log("Received native callback: " + JSON.stringify(response));
    jsCallback.onEvent(response);
}

/**
 * Handle error from native side
 * 
 * @param {*} error 
 */
var nativeErrorCallback = function(error) {
    console.log("Error from native side: " + error);
}

VidyoIOPlugin.prototype.setCallback = function(callback) {
    console.log("Callback to JS has been provided");
    jsCallback = callback;
}

/**
 * Launch conference activity and pass the callbacks
 */
VidyoIOPlugin.prototype.launch = function(args) {
    exec(nativeResponseCallback, nativeErrorCallback, "VidyoIOPlugin", "launchVidyoIO", args);
}

module.exports = new VidyoIOPlugin();
