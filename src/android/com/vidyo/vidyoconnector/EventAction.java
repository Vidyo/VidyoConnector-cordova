package com.vidyo.vidyoconnector;

import android.os.Bundle;

import org.json.JSONObject;

public class EventAction {

    public static final String CONNECTED_EVENT = "Connected";
    public static final String DISCONNECTED_EVENT = "Disconnected";
    public static final String FAILURE_EVENT = "Failure";

    public static final String CAMERA_STATE_EVENT = "CameraStateUpdated";
    public static final String MIC_STATE_EVENT = "MicrophoneStateUpdated";

    private static final String EVENT_KEY = "event";
    private static final String REASON_KEY = "reason";
    private static final String MUTED_KEY = "muted";

    private JSONObject jsonBody;

    private EventAction(JSONObject jsonBody) {
        this.jsonBody = jsonBody;
    }

    public JSONObject getJsonBody() {
        return jsonBody;
    }

    public static EventAction reportEvent(String event, boolean muted) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(MUTED_KEY, muted);
        return reportEvent(event, bundle);
    }

    public static EventAction reportEvent(String event, String reason) {
        Bundle bundle = new Bundle();
        bundle.putString(REASON_KEY, reason);
        return reportEvent(event, bundle);
    }

    private static EventAction reportEvent(String event, Bundle data) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(EVENT_KEY, event);

            if (data != null)
                for (String key : data.keySet()) jsonObject.put(key, data.get(key));

            return new EventAction(jsonObject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}