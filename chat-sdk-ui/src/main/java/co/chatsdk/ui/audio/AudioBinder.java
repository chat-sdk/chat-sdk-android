package co.chatsdk.ui.audio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import co.chatsdk.core.audio.Recording;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ActivityResult;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.core.utils.DisposableMap;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.activities.BaseActivity;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.interfaces.TextInputDelegate;
import co.chatsdk.ui.utils.InfiniteToast;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;

public class AudioBinder {

    protected boolean audioModeEnabled = false;
    protected boolean permissionsGranted = false;

    protected Recording recording = null;

    protected Activity activity;
    protected TextInputDelegate delegate;
    protected DisposableMap dm = new DisposableMap();
    protected ImageButton sendButton;
    protected EditText input;
    protected Drawable sendButtonDrawable;

    @SuppressLint("ClickableViewAccessibility")
    public AudioBinder(Activity activity, ImageButton sendButton, EditText input) {
        this.activity = activity;
        this.sendButton = sendButton;
        this.input = input;

        sendButton.setOnClickListener(v -> {
            if (audioModeEnabled) {

                int requestCode = 8898;
                String filePath = Environment.getExternalStorageDirectory() + "/recorded_audio.wav";

                dm.add(ActivityResultPushSubjectHolder.shared().subscribe(activityResult -> {
                    if (activityResult.requestCode == requestCode) {
                        if (activityResult.resultCode == RESULT_OK) {
                            // Great! User has recorded and saved the audio file
                            File audioFile = new File(filePath);

                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(audioFile.getAbsolutePath());
                            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            int millSecond = Integer.parseInt(durationStr);

                            ChatSDK.audioMessage().sendMessage(audioFile, "audio/wav", millSecond, null);
                        }
                    }
                }));

                int color = activity.getResources().getColor(R.color.primary);
                AndroidAudioRecorder.with(activity)
                        // Required
                        .setFilePath(filePath)
                        .setColor(color)
                        .setRequestCode(requestCode)

                        // Optional
                        .setSource(AudioSource.MIC)
                        .setChannel(AudioChannel.STEREO)
                        .setSampleRate(AudioSampleRate.HZ_48000)
                        .setKeepDisplayOn(true)

                        // Start recording
                        .record();

            }
        });

        dm.add(PermissionRequestHandler.requestRecordAudio(activity).subscribe(() -> {
            permissionsGranted = true;
            updateRecordMode();
        }, throwable -> ToastHelper.show(activity, throwable.getLocalizedMessage())));


        // So we want to be in audio mode if the keyboard is not showing and
        // there is no text in the edit text
        KeyboardVisibilityEvent.setEventListener(activity, isOpen -> {
            updateRecordMode();
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateRecordMode();
            }
        });

    }

    protected void updateRecordMode() {
        if (activity != null && input != null && permissionsGranted) {
            boolean keyboardVisible = KeyboardVisibilityEvent.INSTANCE.isKeyboardVisible(activity);
            boolean isEmpty = input.getText().toString().isEmpty();
            if (keyboardVisible || !isEmpty) {
                endRecordingMode();
            } else {
                startRecordingMode();
            }
        }
    }

    protected void startRecordingMode() {
        if (!audioModeEnabled) {
            sendButtonDrawable = sendButton.getDrawable();
            sendButton.setImageDrawable(Icons.get(Icons.choose().microphone, R.color.white));
            sendButton.setEnabled(true);
            audioModeEnabled = true;
        }
    }

    protected void endRecordingMode() {
        if (audioModeEnabled) {
            sendButton.setImageDrawable(sendButtonDrawable);
            sendButton.setEnabled(!input.getText().toString().isEmpty());
//            sendButton.setImageDrawable(Icons.get(Icons.choose().send, R.color.white));
            audioModeEnabled = false;
        }
    }

}
