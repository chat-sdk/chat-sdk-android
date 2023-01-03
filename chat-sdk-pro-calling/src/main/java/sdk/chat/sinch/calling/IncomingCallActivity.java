package sdk.chat.sinch.calling;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.video.VideoCallListener;

import de.hdodenhof.circleimageview.CircleImageView;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.PermissionRequestHandler;
import sdk.chat.sinch.R;
import sdk.chat.sinch.SinchModule;
import sdk.chat.sinch.SinchService;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.DisposableMap;

public class IncomingCallActivity extends Activity {

    static final String TAG = IncomingCallActivity.class.getSimpleName();
    private String callId;
    private CarAudioPlayer audioPlayer;
    private DisposableMap disposableMap = new DisposableMap();

    private CircleImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvEmail;
    private ImageButton btnAnswer;
    private ImageButton btnDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        ivAvatar = findViewById(R.id.ivAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnAnswer = findViewById(R.id.btnAnswer);
        btnDecline = findViewById(R.id.btnDecline);
        btnAnswer.setOnClickListener(clickListener);
        btnDecline.setOnClickListener(clickListener);

        audioPlayer = new CarAudioPlayer(this);
        audioPlayer.playRingtone();
        callId = getIntent().getStringExtra(SinchService.CALL_ID);

        Call call = sinchService().getCall(callId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
            User remoteUser = ChatSDK.db().fetchUserWithEntityID(call.getRemoteUserId());
            if (remoteUser != null) {
//                Glide.with(this).load(remoteUser.getAvatarURL()).into(ivAvatar);

                Glide.with(this).load(remoteUser.getAvatarURL())
                        .placeholder(R.drawable.icn_100_profile)
                        .fallback(R.drawable.icn_100_profile)
                        .into(ivAvatar);

//                ivAvatar.setImageURI(remoteUser.getAvatarURL());
                tvUsername.setText(remoteUser.getName());
                tvEmail.setText(remoteUser.getEmail());
            } else {
                tvUsername.setText(getResources().getText(R.string.call_anonymous));
                tvEmail.setText("");
            }
        } else {
            Log.e(TAG, "Started with invalid callId, aborting");
            finish();
        }
    }

    private void answerClicked() {
        audioPlayer.stopRingtone();
        Call call = sinchService().getCall(callId);
        if (call != null) {
            disposableMap.add(PermissionRequestHandler.requestRecordAudio(this).subscribe(() -> {
                disposableMap.add(PermissionRequestHandler.requestCameraAccess(this).subscribe(() -> {
                    call.answer();
                    Intent intent = new Intent(this, CallActivity.class);
                    intent.putExtra(SinchService.CALL_ID, callId);
                    startActivity(intent);
                }, throwable -> {
                    ToastHelper.show(this, throwable.getLocalizedMessage());
                }));
            }, throwable -> {
                ToastHelper.show(this, throwable.getLocalizedMessage());
            }));
        } else {
            finish();
        }
    }

    private void declineClicked() {
        audioPlayer.stopRingtone();
        Call call = sinchService().getCall(callId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private class SinchCallListener implements VideoCallListener {

        @Override
        public void onCallEnded(Call call) {
//            ToastHelper.show(getApplicationContext(), getResources().getString(R.string.call_ended));
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            audioPlayer.stopRingtone();
            finish();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
        }

        @Override
        public void onVideoTrackAdded(Call call) {
            // Display some kind of icon showing it's a video call
        }

        @Override
        public void onVideoTrackPaused(Call call) {

        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }
    }

    private OnClickListener clickListener = v -> {
        if (v.equals(btnAnswer)) {
            answerClicked();
        }
        if (v.equals(btnDecline)) {
            declineClicked();
        }
    };

    protected SinchService sinchService() {
        return SinchModule.shared().sinchService;
    }


    @Override
    protected void onStop() {
        super.onStop();
        disposableMap.dispose();
    }

}
