package sdk.chat.sinch;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushTokenRegistrationCallback;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.UserController;
import com.sinch.android.rtc.UserRegistrationCallback;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.video.VideoController;

import org.pmw.tinylog.Logger;

import io.reactivex.disposables.Disposable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.sinch.calling.IncomingCallActivity;

public class SinchService {

    public static final String CALL_ID = "CALL_ID";
    static final String TAG = SinchService.class.getSimpleName();

    private SinchClient mSinchClient;
    private String mUserId;

    private StartFailedListener mListener;
    protected UserController userController;

    public void start(Context context, String userId) {
        if (mSinchClient == null) {
            mUserId = userId;
            mSinchClient = Sinch.getSinchClientBuilder().context(context)
                    .userId(userId)
                    .applicationKey(SinchModule.config().applicationKey)
                    .environmentHost(SinchModule.config().environmentHost)
                    .build();

            mSinchClient.setSupportManagedPush(true);
//            mSinchClient.setSupportCalling(true);
            mSinchClient.startListeningOnActiveConnection();

            mSinchClient.addSinchClientListener(new MySinchClientListener());
            mSinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());
            mSinchClient.start();
        }

        if (userController == null) {
            userController = Sinch.getUserControllerBuilder()
                    .context(context)
                    .applicationKey(SinchModule.config().applicationKey)
                    .userId(userId)
                    .environmentHost(SinchModule.config().environmentHost)
                    .build();
            userController.registerUser(new SinchUserRegistrationCallback(), new SinchPushTokenRegistrationCallback());
        }

    }

    public void stop() {
        if (mSinchClient != null) {
            mSinchClient.terminateGracefully();
            mSinchClient = null;
            mUserId = null;
        }
        if (userController != null) {
            userController.unregisterPushToken(new SinchPushTokenRegistrationCallback());
        }
    }

    public SinchClient client() {
        return mSinchClient;
    }

    private boolean isStarted() {
        return (mSinchClient != null && mSinchClient.isStarted());
    }

    public Call callUserVideo(String userId) {
        Call call = mSinchClient.getCallClient().callUserVideo(userId);
        call.pauseVideo();
        return call;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setStartListener(StartFailedListener listener) {
        mListener = listener;
    }

    public Call getCall(String callId) {
        return mSinchClient.getCallClient().getCall(callId);
    }

    public VideoController getVideoController() {
        if (!isStarted()) return null;
        return mSinchClient.getVideoController();
    }

    public AudioController getAudioController() {
        if (!isStarted()) return null;
        return mSinchClient.getAudioController();
    }

    public interface StartFailedListener {
        void onStartFailed(SinchError error);
        void onStarted();
    }

    private class MySinchClientListener implements SinchClientListener {

        @Override
        public void onClientFailed(SinchClient client, SinchError error) {
            if (mListener != null) mListener.onStartFailed(error);
            mSinchClient.terminateGracefully();
            mSinchClient = null;
        }

        @Override
        public void onClientStarted(SinchClient client) {
            if (mListener != null) mListener.onStarted();
        }

        @Override
        public void onLogMessage(int level, String area, String message) {
            switch (level) {
                case Log.DEBUG:
                    Logger.debug(message);
                    break;
                case Log.ERROR:
                    Logger.error(message);
                    break;
                case Log.INFO:
                    Logger.info(message);
                    break;
                case Log.VERBOSE:
                    Logger.trace(message);
                    break;
                case Log.WARN:
                    Logger.warn(message);
                    break;
            }
        }

        @Override
        public void onPushTokenRegistered() {
            Logger.warn("pushTokenRegistered");
        }

        @Override
        public void onPushTokenRegistrationFailed(SinchError sinchError) {
            Logger.warn("pushTokenFailed", sinchError.getMessage());
        }

        @Override
        public void onCredentialsRequired(ClientRegistration clientRegistration) {
            Disposable d = SinchModule.config().jwtProvider.getJWT(getUserId()).subscribe(s -> {
                if (s != null) {
                    clientRegistration.register(s);
                } else {
                    clientRegistration.registerFailed();
                }
            });
        }

        @Override
        public void onUserRegistered() {
            Logger.warn("onUserRegistered");
        }

        @Override
        public void onUserRegistrationFailed(SinchError sinchError) {
            Logger.warn("onUserRegistrationFailed", sinchError.getMessage());
        }
    }

    private class SinchCallClientListener implements CallClientListener {

        @Override
        public void onIncomingCall(CallClient callClient, Call call) {
            Context context = ChatSDK.ctx();

            Intent intent = new Intent(context, IncomingCallActivity.class);
            intent.putExtra(CALL_ID, call.getCallId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            ChatSDK.ctx().startActivity(intent);

        }
    }

    private class SinchPushTokenRegistrationCallback implements PushTokenRegistrationCallback {

        @Override
        public void onPushTokenRegistered() {
            Logger.warn("Registered");
        }

        @Override
        public void onPushTokenRegistrationFailed(SinchError sinchError) {
            Logger.warn("Failed");
        }
    }

    private class SinchUserRegistrationCallback implements UserRegistrationCallback {

        @Override
        public void onCredentialsRequired(ClientRegistration clientRegistration) {
            Disposable d = SinchModule.config().jwtProvider.getJWT(getUserId()).subscribe(s -> {
                if (s != null) {
                    clientRegistration.register(s);
                } else {
                    clientRegistration.registerFailed();
                }
            });
        }

        @Override
        public void onUserRegistered() {
            Logger.warn("Registered");
        }

        @Override
        public void onUserRegistrationFailed(SinchError sinchError) {
            Logger.warn("Failed");
        }
    }

}
