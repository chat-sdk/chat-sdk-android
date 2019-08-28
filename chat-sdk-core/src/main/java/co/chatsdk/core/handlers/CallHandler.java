package co.chatsdk.core.handlers;

import android.app.Activity;
import android.view.View;

import io.reactivex.Single;

/**
 * Created by Pepe Becker on 27/08/2019.
 */

public interface CallHandler {

    void startWithUserId(String userId);
    Single<Call> callUser(String userId);
    Single<Call> callUser(String userId, Activity activity);
    Call getCall(String entityId);

    View getLocalView();
    View getRemoteView();

    void startCallActivity(String callId);
    void startIncomingCallActivity(String callId);
    void enableSpeaker();
    void disableSpeaker();

    void onIncomingCall(Call call);

    int getCaptureDevicePosition();
    void setCaptureDevicePosition(int i);
    void toggleCaptureDevicePosition();

    Class getCallActivity();
    Class getIncomingCallActivity();
    void setCallActivity(Class callActivity);
    void setIncomingCallActivity(Class incomingCallActivity);

}
