# vidyo.io-connector-cordova

This is a Vidyo.io Android and iOS plugin for Cordova. This plugin is built using the Vidyo.io Android sample available @ https://vidyo.io. 

- [How to use this plugin](#how-to-use)
- [Sample Cordova project using this Plugin](#how-to-import-this-plugin-in-to-a-cordova-project)
- [How to create this plugin from scratch (optional)](#how-to-create-this-plugin-from-scratch)
- [iOS](#ios)


## How to use

      cordova plugin add <path-to-plugin-folder>
      
 OR,
 
      cordova plugin add https://github.com/Vidyo/vidyo.io-connector-cordova.git
      


## How to import this plugin in to a Cordova Project

Here we demontrate how to create a sample Cordova project and import the VidyoIOCordovaPlugin in to that project.

### Prerequisites

- npm - Node.js package manager. I have used NodeJS version 6.9.1. Download from - https://nodejs.org/en/
- Android SDK with Android API 26 (v8.0) installed.
- AndroidStudio if you want to debug the application

### Install Cordova
      $ npm install -g cordova [I have used Cordova 7.0.1]
      
### Create a Cordova project
      $ cordova create VidyoIOHybrid com.vidyo.vidyoiohybrid "VidyoIOHybrid app"

### Add Android platform to this project
      $ cd VidyoIOHybrid
      $ cordova platform add android
    
### Now add the previously built Vidyo.IO plugin to this project
      $ cordova plugin add <plugin-path>  
 OR,
      
      $ cordova plugin add https://github.com/Vidyo/vidyo.io-connector-cordova.git
      
This step copies all the relevant files from the plugin folder to the Cordova project folder. It also merges the information related to permissions in to the AndroidManifest.xml. This step should complete without any errors.

### Make changes to Cordova application UI
Cordova application's main page is rendered using an html file - VidyoIOHybrid/www/index.html. Now we add a button to this html page. Clicking this button will launch the native Vidyo.IO activity. We also add text boxed to collect the information like vidyo resource ID, token, display name and server address.

```
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Security-Policy" content="default-src 'self' data: gap: https://ssl.gstatic.com 'unsafe-eval'; style-src 'self' 'unsafe-inline'; media-src *; img-src 'self' data: content:;">
<meta name="format-detection" content="telephone=no">
<meta name="msapplication-tap-highlight" content="no">
<meta name="viewport" content="user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width">
<link rel="stylesheet" type="text/css" href="css/index.css">
<title>Hello World</title>
</head>
<body>
<div class="app">
<h1>Apache Cordova</h1>
<div id="deviceready" class="blink">
<p class="event listening">Connecting to Device</p>
<p class="event received">Device is Ready</p>
</div>
<p>
<label>Host:</label>
<input type = "text" id = "host" value = "prod.vidyo.io" />
</p>
<p>
<label>Name:</label>
<input type = "text" id = "name" value = "demoUser" />
</p>
<p>
<label>Resource ID:</label>
<input type = "text" id = "resource" value = "demoRoom" />
</p>
<p>
<label>Token:</label>
<input type = "text" id = "token" value = "" />
</p>
<button id = "new_activity">Launch Vidyo</button>
</div>
<script type="text/javascript" src="cordova.js"></script>
<script type="text/javascript" src="js/index.js"></script>
</body>
</html>

```
Next edit the VidyoIOHybrid/www/js/index.js and define the onclick event for the button we just added.

```
var app = {
    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },
 
    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        this.receivedEvent('deviceready');
    },
 
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');
 
        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');
 
        console.log('Received Event: ' + id);
        document.getElementById("new_activity").addEventListener("click", new_activity);
    }
};
 
app.initialize();

var onVidyoEvent = {

    onEvent: function(response) {
        var event = response.event;

        switch(response.event) {
            case "Connected":
                console.log("JS layer: connected to the conference");
                break;

            case "Disconnected":
                var reason = response.reason;
                console.log("JS layer: disconnected from the conference. Reason: " + reason);
                break;
            
            case "Failure":
                var reason = response.reason;
                console.log("JS layer: Failure during connection. Reason: " + reason);
                break; 

            case "CameraStateUpdated":
                var muted = response.muted;
                console.log("JS layer: Received camera state updated. Muted: " + muted);
                break;
                
            case "MicrophoneStateUpdated":
                var muted = response.muted;
                console.log("JS layer: Received microphone state updated. Muted: " + muted);
                break;
        }
    }
}

function new_activity() {
    /* Pass the callback to the native side */
    VidyoIOPlugin.setCallback(onVidyoEvent);
    
    var token = document.getElementById("token").value;
    var host = document.getElementById("host").value;
    var name = document.getElementById("name").value;
    var resource = document.getElementById("resource").value;
    
    VidyoIOPlugin.launch([token,host,name,resource]);
}

/*
 * Disconnect from the call from JS layer. 
 * Would fallback into 'Disconnected' callback 
 */
function disconnect() {
    console.log("JS layer: disconnect requested");
    VidyoIOPlugin.disconnect();
}

/* 
 * Release and close native plugin from JS layer. 
 * You have to disconnect first. This methond won't work otherwise.
 */
function release() {
    console.log("JS layer: release requested");
    VidyoIOPlugin.release();
}

```
#### Android

Build the project

      $ cd VidyoIOHybrid
      $ cordova build android

If the build is successful you can run the application

      $cordova run android
      
You can also run the application by manually installing the apk file from VidyoIOHybrid\platforms\android\build\outputs\apk
On the welcome screen, click on "Launch Vidyo" button to open the Vidyo.IO android activity

## How to create this plugin from scratch (optional)

This step is optional and needed only if you want to build the plugin using a different sample than the one i have used.

This section explains how you can build your own Cordova plugin for Vidyo.io using the Android sample application.

### Prerequisites
- Download the Android SDK from https://vidyo.io. This SDK contains a sample application.
- npm - Node.js package manager. I have used NodeJS version 6.9.1. Download from - https://nodejs.org/en/

#### Install Cordova Plugin manager
>$ npm install -g plugman
 
#### Create plugin project
>$ plugman create --name VidyoIOPlugin --plugin_id com.vidyo.plugin.VidyoIOPlugin --plugin_version 0.0.1

#### Add Android platform to the plugin by typing the following command
>$ cd VidyoIOPlugin

>$ plugman platform add --platform_name android

#### Now we have to copy a few files from Vidyo.IO sample application to the plugin folder.

- copy the res folder from the sample to VidyoIOPlugin/src/android.
- copy the lib folder from the sample to VidyoIOPlugin/src/android
- copy the com folder from VidyoConnector\android\app\src\main\java to VidyoIOPlugin/src/android
- We have to refactor the MainActivity.Java file included in the sample to VidyoIOActivity.java and change the class name also to reflect the change. We have to do this because, when we install this plugin in to the Cordova project, Cordova project also has a MainActivity so there will be a conflict.

Next, edit VidyoIOPlugin/src/android/VidyoIOPlugin.java and make sure it looks like the following. Here we are mapping the method "launchVidyoIO" to invoke Vidyo.io activity. We also pass the required parameters to join a Vidyo room.

```

package com.vidyo.plugin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.vidyo.vidyoconnector.EventAction;
import com.vidyo.vidyoconnector.VidyoIOActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class VidyoIOPlugin extends CordovaPlugin {

    private static final String TAG = "VidyoIOPlugin";

    private static final int PERMISSION_REQ_CODE = 0x7b;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private JSONArray launchVidyoIOArguments;

    private CallbackContext pluginCallback;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("launchVidyoIO")) {

            /* Register to vidyo activity events */
            EventBus.getDefault().register(this);

            /* Store JS callback point */
            this.pluginCallback = callbackContext;

            this.openNewActivity(args);
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /* Unregister from vidyo activity events */
        EventBus.getDefault().unregister(this);
    }

    private void openNewActivity(JSONArray args) throws JSONException {
        Context context = cordova.getActivity().getApplicationContext();

        /* Check for required permissions */
        if (!hasAllPermissions()) {
            this.launchVidyoIOArguments = args;
            this.cordova.requestPermissions(this, PERMISSION_REQ_CODE, PERMISSIONS);
            return;
        }

        Intent intent = new Intent(context, VidyoIOActivity.class);
        intent.putExtra("token", args.getString(0));
        intent.putExtra("host", args.getString(1));
        intent.putExtra("displayName", args.getString(2));
        intent.putExtra("resourceId", args.getString(3));
        intent.putExtra("hideConfig", true);
        intent.putExtra("autoJoin", true);

        this.cordova.getActivity().startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVidyoEvent(EventAction eventAction) {
        if (pluginCallback != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, eventAction.getJsonBody());
            result.setKeepCallback(true);
            pluginCallback.sendPluginResult(result);

            Log.i(TAG, "Event reported: " + eventAction.getJsonBody());
        } else {
            Log.e(TAG, "JS callback context is null.");
        }
    }
    
    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        if (requestCode == PERMISSION_REQ_CODE) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(cordova.getActivity(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                    return; /* quit */
                }
            }

            /* Success */
            if (launchVidyoIOArguments != null) {
                this.openNewActivity(launchVidyoIOArguments);
                this.launchVidyoIOArguments = null;
            }
        }
    }

    private boolean hasAllPermissions() {
        for (String permission : PERMISSIONS) {
            if (!this.cordova.hasPermission(permission)) return false;
        }

        return true;
    }
}

```
Now edit VidyoIOPlugin/www/VidyoIOPlugin.js and create a binding to the native method created in previous step

```
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

/**
 * Disconnect from the conference
 */
VidyoIOPlugin.prototype.disconnect = function() {
    console.log("Trigger disconnect on native side.");
    exec(function(){}, nativeErrorCallback, "VidyoIOPlugin", "disconnect", null);
}

/**
 * Wrap up the plugin screen and release the connector
 */
VidyoIOPlugin.prototype.release = function() {
    console.log("Trigger release on native side.");
    exec(function(){}, nativeErrorCallback, "VidyoIOPlugin", "release", null);
}

module.exports = new VidyoIOPlugin();

```

Next, edit the Cordova plugin configuration file - VidyoIOPlugin/plugin.xml. Here we add rules to correctly merge the permissions, resources, native libraries and Vidyo.IO android activity file. Be sure to change the <clobbers> tag value to VidyoIOPlugin
      
```
<?xml version='1.0' encoding='utf-8'?>
<plugin id="com.vidyo.plugin" version="0.0.1" xmlns="http://apache.org/cordova/ns/plugins/1.0" xmlns:android="http://schemas.android.com/apk/res/android">
<name>VidyoIOPlugin</name>
<js-module name="VidyoIOPlugin" src="www/VidyoIOPlugin.js">
<clobbers target="VidyoIOPlugin" />
</js-module>
<platform name="android">
<config-file parent="/*" target="res/xml/config.xml">
<feature name="VidyoIOPlugin"><param name="android-package" value="com.vidyo.plugin.VidyoIOPlugin" />
</feature>
</config-file>
<config-file parent="/manifest" target="AndroidManifest.xml">
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.CAPTURE_AUDIO_OUTPUT" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-feature android:name="android.hardware.camera" />
</config-file>
<config-file parent="/manifest/application" target="AndroidManifest.xml">
<activity
android:name="com.vidyo.vidyoconnector.VidyoIOActivity"
android:launchMode="singleTop"
android:configChanges="orientation|screenSize"
android:label="@string/app_name" >
<intent-filter>
<category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
</activity>
</config-file>
<source-file src="src/android/VidyoIOPlugin.java" target-dir="src/com/vidyo/plugin" />
<source-file src="src/android/com/vidyo/vidyoconnector/VidyoIOActivity.java" target-dir="src/com/vidyo/vidyoconnector" />
<source-file src="src/android/com/vidyo/vidyoconnector/VidyoConnector.java" target-dir="src/com/vidyo/vidyoconnector" />
<source-file src="src/android/com/vidyo/vidyoconnector/EventAction.java" target-dir="src/com/vidyo/vidyoconnector" />
<source-file src="src/android/com/vidyo/vidyoconnector/TriggerAction.java" target-dir="src/com/vidyo/vidyoconnector" />
<source-file src="src/android/com/vidyo/vidyoconnector/Logger.java" target-dir="src/com/vidyo/vidyoconnector" />

<source-file src="src/android/lib/android/vidyoclient.jar" target-dir="libs" />
<source-file src="src/android/lib/android/arm64-v8a/libVidyoClient.so" target-dir="libs/arm64-v8a" />
<source-file src="src/android/lib/android/armeabi-v7a/libVidyoClient.so" target-dir="libs/armeabi-v7a" />
<source-file src="src/android/lib/android/x86/libVidyoClient.so" target-dir="libs/x86" />
<source-file src="src/android/lib/android/x86_64/libVidyoClient.so" target-dir="libs/x86_64" />
<source-file src="src/android/res/drawable/callend.png" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/callstart.png" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/camera_off.png" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/camera_switch.png" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/cameraonwhite.png" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/microphoneoff.png" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/microphoneonwhite.png" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/rounded_border_edittext.xml" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/toggle_camera_privacy.xml" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/toggle_connect.xml" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/toggle_mic_privacy.xml" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/vidyo_io_icon.png" target-dir="res/drawable" />
<source-file src="src/android/res/drawable/vidyo_io_vertical_dark_at_2x.png" target-dir="res/drawable" />
<source-file src="src/android/res/layout/activity_main.xml" target-dir="res/layout" />
<source-file src="src/android/res/mipmap-hdpi/ic_launcher.png" target-dir="res/mipmap-hdpi" />
<source-file src="src/android/res/mipmap-mdpi/ic_launcher.png" target-dir="res/mipmap-mdpi" />
<source-file src="src/android/res/mipmap-xhdpi/ic_launcher.png" target-dir="res/mipmap-xhdpi" />
<source-file src="src/android/res/mipmap-xxhdpi/ic_launcher.png" target-dir="res/mipmap-xxhdpi" />
<source-file src="src/android/res/values/dimens.xml" target-dir="res/values" />
<source-file src="src/android/res/values/styles.xml" target-dir="res/values" />
<source-file src="src/android/res/values-w820dp/dimens.xml" target-dir="res/values-w820dp" />
<framework src="com.android.support:appcompat-v7:23.0.0" />
<framework src="org.greenrobot:eventbus:3.1.1" />
</platform>
</plugin>

```

Finally create the plugin package file so that it can be imported in to a Cordova project

> plugman createpackagejson /path/to/your/pluginfolder

> Example: plugman createpackagejson C:\CordovaPlugins\VidyoIOPlugin

Your plugin is Ready!

## iOS

### Prerequisites

- MacOS
- Xcode if you want to debug the application
- iOS Vidyo SDK (https://developer.vidyo.io/packages and move VidyoClientIOS.framework in src/ios/lib folder)

#### Add iOS platform to the plugin by typing the following command
>$ cd VidyoIOPlugin

>$ plugman platform add --platform_name ios

#### After plugin added to the app
In Xcode, click on project, in General settings, add the following to "Linked Frameworks and Libraries":
- VidyoClientIOS.framework
- AVFoundation.framework
- AudioToolbox.framework
- CoreLocation.framework
- CoreMedia.framework
- SystemConfiguration.framework
- UIKit.framework

Make sure you have an Entitlements file with : 
```
<dict>
    <key>keychain-access-groups</key>
    <array>
      <string>$(AppIdentifierPrefix)VidyoLicense</string>
      <string>$(AppIdentifierPrefix)com.example.VidyoConnector</string>
    </array>
 </dict>
 ```
 replace "com.example.VidyoConnector" by your Application ID
 
 Now you can build the project
