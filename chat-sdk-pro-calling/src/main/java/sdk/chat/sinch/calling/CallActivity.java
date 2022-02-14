package sdk.chat.sinch.calling;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallDirection;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.PermissionRequestHandler;
import sdk.chat.sinch.R;
import sdk.chat.sinch.SinchModule;
import sdk.chat.sinch.SinchService;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.DisposableMap;

public class CallActivity extends Activity {

    static final String CALL_ESTABLISHED = "callEstablished";
    static final String CALL_START_TIME = "callStartTime";
    static final String ADDED_LISTENER = "addedListener";
    static final String SPEAKER_ENABLED = "speakerEnabled";
    static final String VIDEO_ENABLED = "videoEnabled";
    static final String SHOW_REMOTE_VIDEO = "showRemoteVideo";

    private CarAudioPlayer audioPlayer;
    private Timer timer;
    private UpdateCallDurationTask durationTask;

    private String callId;
    private long callStart = 0;
    private boolean callEstablished = false;
    private boolean addedListener = false;
    private boolean speakerEnabled = false;
    private boolean videoEnabled = false;
    private boolean showRemoteVideo = false;
    private boolean isInitiator = false;

    private LinearLayout llUserDetails;
    private CircleImageView ivAvatar;
    private TextView tvCallState;
    private TextView tvRemoteUser;
    private ImageButton btnHangup;
    private ImageButton btnToggleSpeaker;
    private ImageButton btnToggleVideo;

    private DisposableMap disposableList = new DisposableMap();
    private Gson gson = new Gson();

    private class UpdateCallDurationTask extends TimerTask {
        @Override
        public void run() {
            CallActivity.this.runOnUiThread(CallActivity.this::updateCallDuration);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(CALL_ESTABLISHED, callEstablished);
        savedInstanceState.putLong(CALL_START_TIME, callStart);
        savedInstanceState.putBoolean(ADDED_LISTENER, addedListener);
        savedInstanceState.putBoolean(SPEAKER_ENABLED, speakerEnabled);
        savedInstanceState.putBoolean(VIDEO_ENABLED, videoEnabled);
        savedInstanceState.putBoolean(SHOW_REMOTE_VIDEO, showRemoteVideo);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        callEstablished = savedInstanceState.getBoolean(CALL_ESTABLISHED);
        callStart = savedInstanceState.getLong(CALL_START_TIME);
        addedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
        speakerEnabled = savedInstanceState.getBoolean(SPEAKER_ENABLED);
        videoEnabled = savedInstanceState.getBoolean(VIDEO_ENABLED);
        showRemoteVideo = savedInstanceState.getBoolean(SHOW_REMOTE_VIDEO);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_screen);

        audioPlayer = new CarAudioPlayer(this);
        llUserDetails = findViewById(R.id.llUserDetails);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvRemoteUser = findViewById(R.id.tvUsername);
        tvCallState = findViewById(R.id.tvCallState);
        btnHangup = findViewById(R.id.btnHangup);
        btnToggleSpeaker = findViewById(R.id.btnSpeaker);
        btnToggleVideo = findViewById(R.id.btnVideo);

        btnHangup.setOnClickListener(v -> endCall());

        btnToggleSpeaker.setVisibility(View.GONE);
        btnToggleVideo.setVisibility(View.GONE);

        btnToggleSpeaker.setOnClickListener(v -> setSpeakerEnabled(!speakerEnabled));
        btnToggleVideo.setOnClickListener(v -> setVideoEnabled(!videoEnabled));

        callId = getIntent().getStringExtra(SinchService.CALL_ID);
        if (savedInstanceState == null) {
            callStart = System.currentTimeMillis();
        }

        // Get the call
        Call call = sinchService().getCall(callId);
        if (call != null) {
            if (!addedListener) {
                call.addCallListener(new SinchCallListener());
                addedListener = true;
            }
            if (call.getDirection() == CallDirection.OUTGOING) {
                isInitiator = true;
            }
        } else {
            ChatSDK.events().onError(new Error("Started with invalid callId, aborting."));
            finish();
        }

        if (speakerEnabled) {
            setSpeakerEnabled(true);
        }

        if (videoEnabled) {
            setVideoEnabled(true);
        }

        if (showRemoteVideo) {
            addRemoteVideoView();
        }

        if (callEstablished) {
            startDurationTimer();
            btnToggleSpeaker.setVisibility(View.VISIBLE);
            btnToggleVideo.setVisibility(View.VISIBLE);
        }

        updateUI();

    }

    protected SinchService sinchService() {
        return SinchModule.shared().sinchService;
    }

    private void setSpeakerEnabled(boolean enabled) {
        AudioController audioController = sinchService().getAudioController();
        if (enabled) {
            audioController.enableSpeaker();
            btnToggleSpeaker.setImageResource(R.drawable.baseline_volume_up_24);
        } else {
            audioController.disableSpeaker();
            btnToggleSpeaker.setImageResource(R.drawable.baseline_volume_off_24);
        }
        speakerEnabled = enabled;
    }

    private void setVideoEnabled(boolean enabled) {
        Call call = sinchService().getCall(callId);
        if (call != null) {
            if (enabled) {
                disposableList.add(PermissionRequestHandler.requestCameraAccess(this).subscribe(() -> {
                    call.resumeVideo();
                    addLocalVideoView();
                    btnToggleVideo.setImageResource(R.drawable.baseline_videocam_24);
                }, throwable -> {
                    ToastHelper.show(this, throwable.getLocalizedMessage());
                }));
            } else {
                removeLocalVideoView();
                call.pauseVideo();
                btnToggleVideo.setImageResource(R.drawable.baseline_videocam_off_24);
            }
            videoEnabled = enabled;
        }
    }

    //method to update video feeds in the UI
    private void updateUI() {
        if (sinchService() == null) return;

        Call call = sinchService().getCall(callId);
        if (call != null) {
            User remoteUser = ChatSDK.db().fetchUserWithEntityID(call.getRemoteUserId());
            if (remoteUser != null) {

                Glide.with(this).load(remoteUser.getAvatarURL())
                        .placeholder(R.drawable.icn_100_profile)
                        .fallback(R.drawable.icn_100_profile)
                        .into(ivAvatar);

//                ivAvatar.setImageURI(remoteUser.getAvatarURL());
                tvRemoteUser.setText(remoteUser.getName());
            } else {
                tvRemoteUser.setText(getResources().getText(R.string.call_anonymous));
            }
            if (call.getState() == CallState.INITIATING || call.getState() == CallState.PROGRESSING) {
                tvCallState.setText(R.string.call_progressing);
            }
        }
    }

    private boolean isViewAdded(int resourceId) {
        return findViewById(resourceId) != null;
    }

    //stop the timer when call is ended
    @Override
    public void onStop() {
        super.onStop();
        if (durationTask != null) durationTask.cancel();
        if (timer != null) timer.cancel();
        removeLocalVideoView();
        removeRemoteVideoView();

//        Call call = sinchService().getCall(callId);
//        if (call != null) {
//            String callsJson = ChatSDK.currentUser().metaStringForKey("calls");
//            ArrayList<CarCallListItem> calls = new ArrayList<>();
//            if (callsJson != null) {
//                calls = gson.fromJson(callsJson, new TypeToken<List<CarCallListItem>>(){}.getType());
//            }
//            User user = ChatSDK.db().fetchUserWithEntityID(call.getRemoteUserId());
//            if (user != null) {
//                calls.add(new CarCallListItem(user.getName(), user.getEmail(), user.getEntityID(), user.getAvatarURL(), getCallDuration(), isInitiator));
//                callsJson = gson.toJson(calls);
//                ChatSDK.currentUser().setMetaString("calls", callsJson);
//                disposableList.add(ChatSDK.core().pushUser()
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe());
//            }
//        }
    }

    //start the timer for the call duration here
    @Override
    public void onStart() {
        super.onStart();
        updateUI();
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposableList.dispose();
    }

    //method to end the call
    private void endCall() {
        audioPlayer.stopProgressTone();
        Call call = sinchService().getCall(callId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private void startDurationTimer() {
        timer = new Timer();
        durationTask = new UpdateCallDurationTask();
        timer.schedule(durationTask, 0, 500);
    }

    private String formatTimespan(long timespan) {
        long totalSeconds = timespan / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private long getCallDuration() {
        return System.currentTimeMillis() - callStart;
    }

    //method to update live duration of the call
    private void updateCallDuration() {
        if (callStart > 0) {
            tvCallState.setText(formatTimespan(getCallDuration()));
        }
    }

    private void setViewWidth(View view, int width) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        final float density =  getResources().getDisplayMetrics().density;
        params.width = (int) (width * density + 0.5f);
        view.setLayoutParams(params);
    }

    private void addLocalVideoView() {
        if (sinchService() == null) return;

        final VideoController vc = sinchService().getVideoController();
        if (vc != null && vc.getLocalView().getParent() == null) {
            LinearLayout localView = findViewById(R.id.llLocalVideo);
            localView.addView(vc.getLocalView());
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setViewWidth(localView, 200);
            } else {
                setViewWidth(localView, 150);
            }
            localView.setOnClickListener(v -> vc.toggleCaptureDevicePosition());
        }
    }

    private void removeLocalVideoView() {
        if (sinchService() == null) return;

        VideoController vc = sinchService().getVideoController();
        if (vc != null && vc.getLocalView().getParent() != null) {
            LinearLayout localView = findViewById(R.id.llLocalVideo);
            localView.removeView(vc.getLocalView());
            setViewWidth(localView, 0);
        }
    }

    private void addRemoteVideoView() {
        if (sinchService() == null) return;

        final VideoController vc = sinchService().getVideoController();
        if (vc != null && vc.getRemoteView().getParent() == null) {
            LinearLayout remoteView = findViewById(R.id.llRemoteVideo);
            remoteView.addView(vc.getRemoteView());
        }

        llUserDetails.setVisibility(View.INVISIBLE);
    }

    private void removeRemoteVideoView() {
        if (sinchService() == null) return;

        VideoController vc = sinchService().getVideoController();
        if (vc != null && vc.getRemoteView().getParent() != null) {
            LinearLayout remoteView = findViewById(R.id.llRemoteVideo);
            remoteView.removeView(vc.getRemoteView());
        }

        llUserDetails.setVisibility(View.VISIBLE);
    }

    private class SinchCallListener implements VideoCallListener {

        @Override
        public void onCallEnded(Call call) {
            audioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            callEstablished = true;
            audioPlayer.stopProgressTone();
            startDurationTimer();
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            callStart = System.currentTimeMillis();

            btnToggleSpeaker.setVisibility(View.VISIBLE);
            btnToggleVideo.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCallProgressing(Call call) {
            audioPlayer.playProgressTone();
        }

//        @Override
//        public void onShouldSendPushNotification(Call call, List pushPairs) {
//            // Send a push through your push provider here, e.g. GCM
//        }

        @Override
        public void onVideoTrackAdded(Call call) {}

        @Override
        public void onVideoTrackPaused(Call call) {
            removeRemoteVideoView();
            showRemoteVideo = false;
        }

        @Override
        public void onVideoTrackResumed(Call call) {
            addRemoteVideoView();
            showRemoteVideo = true;
        }

    }

}
