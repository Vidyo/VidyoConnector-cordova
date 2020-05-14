package com.vidyo.vidyoconnector;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.vidyo.VidyoClient.Connector.Connector;
import com.vidyo.VidyoClient.Connector.ConnectorPkg;
import com.vidyo.VidyoClient.Device.Device;
import com.vidyo.VidyoClient.Device.LocalCamera;
import com.vidyo.VidyoClient.Endpoint.LogRecord;

import com.vidyo.VidyoClient.Endpoint.Participant;
import com.vidyo.vidyoiohybrid.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class VidyoIOActivity extends Activity implements Connector.IConnect, Connector.IRegisterLogEventListener,
        Connector.IRegisterLocalCameraEventListener, Connector.IRegisterParticipantEventListener {

    enum VIDYO_CONNECTOR_STATE {
        VC_CONNECTED,
        VC_DISCONNECTED,
        VC_DISCONNECTED_UNEXPECTED,
        VC_CONNECTION_FAILURE
    }

    private VIDYO_CONNECTOR_STATE mVidyoConnectorState = VIDYO_CONNECTOR_STATE.VC_DISCONNECTED;

    private boolean mVidyoConnectorConstructed = false;
    private boolean mVidyoClientInitialized = false;

    private Logger mLogger = Logger.getInstance();

    private Connector mVidyoConnector = null;

    private ToggleButton mToggleConnectButton;
    private ProgressBar mConnectionSpinner;
    private LinearLayout mControlsLayout;
    private LinearLayout mToolbarLayout;
    private EditText mHost;
    private EditText mDisplayName;
    private EditText mToken;
    private EditText mResourceId;
    private TextView mToolbarStatus;
    private FrameLayout mVideoFrame;
    private FrameLayout mToggleToolbarFrame;

    private boolean mHideConfig = false;
    private boolean mAutoJoin = false;
    private boolean mAllowReconnect = true;

    private boolean mIsCameraPrivacyOn = false;

    private String mReturnURL = null;

    private VidyoIOActivity mSelf;

    /*
     *  Operating System Events
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLogger.Log("onCreate");
        super.onCreate(savedInstanceState);
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        EventBus.getDefault().register(this);

        setContentView(R.layout.activity_main);

        // Initialize the member variables
        mToggleConnectButton = (ToggleButton) findViewById(R.id.toggleConnectButton);
        mControlsLayout = (LinearLayout) findViewById(R.id.controlsLayout);
        mToolbarLayout = (LinearLayout) findViewById(R.id.toolbarLayout);
        mVideoFrame = (FrameLayout) findViewById(R.id.videoFrame);
        mToggleToolbarFrame = (FrameLayout) findViewById(R.id.toggleToolbarFrame);
        mHost = (EditText) findViewById(R.id.hostTextBox);
        mDisplayName = (EditText) findViewById(R.id.displayNameTextBox);
        mToken = (EditText) findViewById(R.id.tokenTextBox);
        mResourceId = (EditText) findViewById(R.id.resourceIdTextBox);
        mToolbarStatus = (TextView) findViewById(R.id.toolbarStatusText);
        mConnectionSpinner = (ProgressBar) findViewById(R.id.connectionSpinner);
        mSelf = this;

        // Suppress keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Initialize the VidyoClient
        ConnectorPkg.setApplicationUIContext(this);

        mVidyoClientInitialized = ConnectorPkg.initialize();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mLogger.Log("onNewIntent");
        super.onNewIntent(intent);

        // New intent was received so set it to use in onStart()
        setIntent(intent);
    }

    @Override
    protected void onStart() {
        mLogger.Log("onStart");
        super.onStart();

        // If the app was launched by a different app, then get any parameters; otherwise use default settings
        Intent intent = getIntent();
        mHost.setText(intent.hasExtra("host") ? intent.getStringExtra("host") : "prod.vidyo.io");
        mToken.setText(intent.hasExtra("token") ? intent.getStringExtra("token") : "cHJvdmlzaW9uAHN1bWl0QGRkYTg0MC52aWR5by5pbwA2MzY1ODg4MTAyOAAAMTYyNGJmM2IwMGQ3ODdjNmFkNGIyNzE3YTgxNTlkMGUzMDA3NzQxZTZkOWQzYjEwMTY4ZTMxMWZhZDE4MmMxNmVmOWQwNmQyZDQxMGZjMGUzMzUyZDg5ZWQ5Mzk5NDc3");
        mDisplayName.setText(intent.hasExtra("displayName") ? intent.getStringExtra("displayName") : "");
        mResourceId.setText(intent.hasExtra("resourceId") ? intent.getStringExtra("resourceId") : "demoRoom");
        mReturnURL = intent.hasExtra("returnURL") ? intent.getStringExtra("returnURL") : null;
        mHideConfig = intent.getBooleanExtra("hideConfig", false);
        mAutoJoin = intent.getBooleanExtra("autoJoin", false);
        mAllowReconnect = intent.getBooleanExtra("allowReconnect", true);

        mLogger.Log("onStart: hideConfig = " + mHideConfig + ", autoJoin = " + mAutoJoin + ", allowReconnect = " + mAllowReconnect);

        // Enable toggle connect button
        mToggleConnectButton.setEnabled(true);

        // Hide the controls if hideConfig enabled
        if (mHideConfig) {
            mControlsLayout.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        mLogger.Log("onResume");
        super.onResume();

        ViewTreeObserver viewTreeObserver = mVideoFrame.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mVideoFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // If the vidyo connector was not previously successfully constructed then construct it

                    if (!mVidyoConnectorConstructed) {

                        if (mVidyoClientInitialized) {

                            mVidyoConnector = new Connector(mVideoFrame,
                                    Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default,
                                    16,
                                    "debug@VidyoClient info@VidyoConnector warning ",
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/VidyoIOAndroid.log",
                                    0);
                            mLogger.Log("Version is " + mVidyoConnector.getVersion());

                            if (mVidyoConnector != null) {
                                mVidyoConnectorConstructed = true;

                                // Set initial position
                                RefreshUI();

                                // Register for log callbacks
                                if (!mVidyoConnector.registerLogEventListener(mSelf, "info@VidyoClient info@VidyoConnector warning")) {
                                    mLogger.Log("VidyoConnector RegisterLogEventListener failed");
                                }
                                if (!mVidyoConnector.registerLocalCameraEventListener(mSelf)) {
                                    mLogger.Log("VidyoConnector RegisterLocalCameraEventListener failed");
                                }
                            } else {
                                mLogger.Log("VidyoConnector Construction failed - cannot connect...");
                            }
                        } else {
                            mLogger.Log("ERROR: VidyoClientInitialize failed - not constructing VidyoConnector ...");
                        }

                        Logger.getInstance().Log("onResume: mVidyoConnectorConstructed => " + (mVidyoConnectorConstructed ? "success" : "failed"));
                    }

                    // Resume camera privacy if selected
                    if (mVidyoConnector != null) {
                        mVidyoConnector.setCameraPrivacy(mIsCameraPrivacyOn);
                    }

                    // If configured to auto-join, then simulate a click of the toggle connect button
                    if (mVidyoConnectorConstructed && mAutoJoin && mVidyoConnectorState == VIDYO_CONNECTOR_STATE.VC_DISCONNECTED) {
                        mToggleConnectButton.performClick();
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        mLogger.Log("onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        mLogger.Log("onRestart");
        super.onRestart();
        mVidyoConnector.setMode(Connector.ConnectorMode.VIDYO_CONNECTORMODE_Foreground);

    }

    @Override
    protected void onStop() {
        mLogger.Log("onStop");
        if (mVidyoConnector != null) {
            mVidyoConnector.setMode(Connector.ConnectorMode.VIDYO_CONNECTORMODE_Background);
            mVidyoConnector.setCameraPrivacy(true);
        }
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActionRequested(TriggerAction triggerAction) {
        switch (triggerAction) {
            case DISCONNECT:
                if (mVidyoConnector != null) {
                    triggerDisconnect();
                }
                break;
            case RELEASE:
                /* Wrap up the plugin activity by destroying it. This would call onDestroy and release the connector. */
                if (mVidyoConnector != null && mVidyoConnectorState != VIDYO_CONNECTOR_STATE.VC_CONNECTED) {
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mLogger.Log("onDestroy");
        // ConnectorPkg.uninitialize();

        if (mVidyoConnector != null) {
            mVidyoConnector.unregisterLogEventListener();
            mVidyoConnector.unregisterLocalCameraEventListener();

            mVidyoConnector.disable();
            mVidyoConnector = null;
        }

        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    // The device interface orientation has changed
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mLogger.Log("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

        // Refresh the video size after it is painted
        ViewTreeObserver viewTreeObserver = mVideoFrame.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mVideoFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // Width/height values of views not updated at this point so need to wait
                    // before refreshing UI

                    RefreshUI();
                }
            });
        }
    }

    /*
     * Private Utility Functions
     */

    // Refresh the UI
    private void RefreshUI() {
        // Refresh the rendering of the video
        mVidyoConnector.showViewAt(mVideoFrame, 0, 0, mVideoFrame.getWidth(), mVideoFrame.getHeight());
        mLogger.Log("VidyoConnectorShowViewAt: x = 0, y = 0, w = " + mVideoFrame.getWidth() + ", h = " + mVideoFrame.getHeight());
    }

    // The state of the VidyoConnector connection changed, reconfigure the UI.
    // If connected, dismiss the controls layout
    private void ConnectorStateUpdated(VIDYO_CONNECTOR_STATE state, final String statusText) {
        mLogger.Log("ConnectorStateUpdated, state = " + state.toString());

        mVidyoConnectorState = state;

        // Execute this code on the main thread since it is updating the UI layout

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Update the toggle connect button to either start call or end call image
                mToggleConnectButton.setChecked(mVidyoConnectorState == VIDYO_CONNECTOR_STATE.VC_CONNECTED);

                // Set the status text in the toolbar
                mToolbarStatus.setText(statusText);

                if (mVidyoConnectorState == VIDYO_CONNECTOR_STATE.VC_CONNECTED) {
                    // Enable the toggle toolbar control
                    mToggleToolbarFrame.setVisibility(View.VISIBLE);

                    if (!mHideConfig) {
                        // Update the view to hide the controls
                        mControlsLayout.setVisibility(View.GONE);
                    }
                } else {
                    // VidyoConnector is disconnected

                    // Disable the toggle toolbar control
                    mToggleToolbarFrame.setVisibility(View.GONE);

                    // If a return URL was provided as an input parameter, then return to that application
                    if (mReturnURL != null) {
                        // Provide a callstate of either 0 or 1, depending on whether the call was successful
                        Intent returnApp = getPackageManager().getLaunchIntentForPackage(mReturnURL);
                        returnApp.putExtra("callstate", (mVidyoConnectorState == VIDYO_CONNECTOR_STATE.VC_DISCONNECTED) ? 1 : 0);
                        startActivity(returnApp);
                    }

                    // If the allow-reconnect flag is set to false and a normal (non-failure) disconnect occurred,
                    // then disable the toggle connect button, in order to prevent reconnection.
                    if (!mAllowReconnect && (mVidyoConnectorState == VIDYO_CONNECTOR_STATE.VC_DISCONNECTED)) {
                        mToggleConnectButton.setEnabled(false);
                        mToolbarStatus.setText("Call ended");
                    }

                    if (!mHideConfig) {
                        // Update the view to display the controls
                        mControlsLayout.setVisibility(View.VISIBLE);
                    }
                }

                // Hide the spinner animation
                mConnectionSpinner.setVisibility(View.INVISIBLE);
            }
        });
    }

    /*
     * Button Event Callbacks
     */

    // The Connect button was pressed.
    // If not in a call, attempt to connect to the backend service.
    // If in a call, disconnect.
    public void ToggleConnectButtonPressed(View v) {
        if (mToggleConnectButton.isChecked()) {
            mToolbarStatus.setText("Connecting...");

            // Display the spinner animation
            mConnectionSpinner.setVisibility(View.VISIBLE);

            final boolean status = mVidyoConnector.connect(
                    mHost.getText().toString(),
                    mToken.getText().toString(),
                    mDisplayName.getText().toString(),
                    mResourceId.getText().toString(),
                    this);
            if (!status) {
                // Hide the spinner animation
                mConnectionSpinner.setVisibility(View.INVISIBLE);

                ConnectorStateUpdated(VIDYO_CONNECTOR_STATE.VC_CONNECTION_FAILURE, "Connection failed");
            }
            mLogger.Log("VidyoConnectorConnect status = " + status);
        } else {
            triggerDisconnect();
        }
    }

    private void triggerDisconnect() {
        // The button just switched to the callStart image: The user is either connected to a resource
        // or is in the process of connecting to a resource; call VidyoConnectorDisconnect to either disconnect
        // or abort the connection attempt.
        // Change the button back to the callEnd image because do not want to assume that the Disconnect
        // call will actually end the call. Need to wait for the callback to be received
        // before swapping to the callStart image.
        mToggleConnectButton.setChecked(true);

        mToolbarStatus.setText("Disconnecting...");

        mVidyoConnector.disconnect();
    }

    // Toggle the microphone privacy
    public void MicrophonePrivacyButtonPressed(View v) {
        boolean privacy = ((ToggleButton) v).isChecked();
        mVidyoConnector.setMicrophonePrivacy(privacy);

        EventBus.getDefault().post(EventAction.reportEvent(EventAction.MIC_STATE_EVENT, privacy));
    }

    // Toggle the camera privacy
    public void CameraPrivacyButtonPressed(View v) {
        mIsCameraPrivacyOn = ((ToggleButton) v).isChecked();
        mVidyoConnector.setCameraPrivacy(mIsCameraPrivacyOn);

        EventBus.getDefault().post(EventAction.reportEvent(EventAction.CAMERA_STATE_EVENT, mIsCameraPrivacyOn));
    }

    // Handle the camera swap button being pressed. Cycle the camera.
    public void CameraSwapButtonPressed(View v) {
        mVidyoConnector.cycleCamera();
    }

    // Toggle visibility of the toolbar
    public void ToggleToolbarVisibility(View v) {
        if (mVidyoConnectorState == VIDYO_CONNECTOR_STATE.VC_CONNECTED) {
            if (mToolbarLayout.getVisibility() == View.VISIBLE) {
                mToolbarLayout.setVisibility(View.INVISIBLE);
            } else {
                mToolbarLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    /*
     *  Connector Events
     */

    @Override
    public void onSuccess() {
        EventBus.getDefault().post(EventAction.reportEvent(EventAction.CONNECTED_EVENT, null));

        mLogger.Log("OnSuccess: successfully connected.");
        ConnectorStateUpdated(VIDYO_CONNECTOR_STATE.VC_CONNECTED, "Connected");
    }

    @Override
    public void onFailure(Connector.ConnectorFailReason reason) {
        EventBus.getDefault().post(EventAction.reportEvent(EventAction.FAILURE_EVENT, reason.name()));

        mLogger.Log("OnFailure: connection attempt failed, reason = " + reason.toString());

        // Update UI to reflect connection failed
        ConnectorStateUpdated(VIDYO_CONNECTOR_STATE.VC_CONNECTION_FAILURE, "Connection failed");
    }

    @Override
    public void onDisconnected(Connector.ConnectorDisconnectReason reason) {
        EventBus.getDefault().post(EventAction.reportEvent(EventAction.DISCONNECTED_EVENT, reason.name()));

        if (reason == Connector.ConnectorDisconnectReason.VIDYO_CONNECTORDISCONNECTREASON_Disconnected) {
            mLogger.Log("OnDisconnected: successfully disconnected, reason = " + reason.toString());
            ConnectorStateUpdated(VIDYO_CONNECTOR_STATE.VC_DISCONNECTED, "Disconnected");
        } else {
            mLogger.Log("OnDisconnected: unexpected disconnection, reason = " + reason.toString());
            ConnectorStateUpdated(VIDYO_CONNECTOR_STATE.VC_DISCONNECTED_UNEXPECTED, "Unexpected disconnection");
        }
    }


    @Override
    public void onLog(LogRecord logRecord) {
        mLogger.Log(logRecord.message);
    }

    @Override
    public void onLocalCameraAdded(LocalCamera localCamera) {

    }

    @Override
    public void onLocalCameraRemoved(LocalCamera localCamera) {

    }

    @Override
    public void onLocalCameraSelected(LocalCamera localCamera) {

    }

    @Override
    public void onLocalCameraStateUpdated(LocalCamera localCamera, Device.DeviceState deviceState) {

    }

    /*
     *  Participant Action Events
     */

    @Override
    public void onParticipantJoined(Participant participant) {
        // Handle participant joined
    }

    @Override
    public void onParticipantLeft(Participant participant) {
        // Handle participant left
    }

    @Override
    public void onDynamicParticipantChanged(ArrayList<Participant> arrayList) {

    }

    @Override
    public void onLoudestParticipantChanged(Participant participant, boolean b) {

    }
}
