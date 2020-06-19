package sdk.chat.ui.audio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.text.Editable;
import android.text.TextWatcher;

import com.stfalcon.chatkit.messages.MessageInput;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.pmw.tinylog.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.FileManager;
import sdk.chat.core.utils.ActivityResultPushSubjectHolder;
import sdk.chat.core.utils.PermissionRequestHandler;
import sdk.chat.ui.R;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.interfaces.TextInputDelegate;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.DisposableMap;

import static android.app.Activity.RESULT_OK;

public class AudioBinder {

    protected boolean audioModeEnabled = false;
    protected boolean permissionsGranted = false;
    protected boolean replyViewShowing = false;

    protected Activity activity;
    protected TextInputDelegate delegate;
    protected DisposableMap dm = new DisposableMap();
    protected MessageInput messageInput;
    protected Drawable sendButtonDrawable;

    @SuppressLint("ClickableViewAccessibility")
    public AudioBinder(Activity activity, TextInputDelegate delegate, MessageInput messageInput) {
        this.activity = activity;
        this.messageInput = messageInput;
        this.delegate = delegate;

        sendButtonDrawable = messageInput.getButton().getDrawable();

        messageInput.setInputListener(input -> {
            if (audioModeEnabled) {

                int requestCode = 8898;

                FileManager fm = ChatSDK.shared().fileManager();
                File audioFile = fm.newDatedFile(fm.audioStorage(), "voice_message", "wav");

                dm.add(ActivityResultPushSubjectHolder.shared().subscribe(activityResult -> {
                    if (activityResult.requestCode == requestCode) {
                        if (activityResult.resultCode == RESULT_OK) {
                            // Great! User has recorded and saved the audio file

                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(audioFile.getAbsolutePath());
                            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            int millSecond = Integer.parseInt(durationStr);

                            delegate.sendAudio(audioFile, "audio/wav", TimeUnit.MILLISECONDS.toSeconds(millSecond));
                            dm.dispose();
                        }
                    }
                }));

                int color = activity.getResources().getColor(R.color.primary);

                AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
                audioManager.requestAudioFocus(focusChange -> {
                    if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        Logger.debug("");
                    }
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        Logger.debug("");
                    }
                }, AudioManager.MODE_NORMAL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

                AndroidAudioRecorder.with(activity)
                        // Required
                        .setFilePath(audioFile.getPath())
                        .setColor(color)
                        .setRequestCode(requestCode)

                        // Optional
                        .setSource(AudioSource.MIC)
                        .setChannel(AudioChannel.MONO)
                        .setSampleRate(AudioSampleRate.HZ_16000)
                        .setKeepDisplayOn(true)

                        // Start recording
                        .record();

            } else {
                delegate.sendMessage(String.valueOf(input));
            }
            return true;
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

        messageInput.getInputEditText().addTextChangedListener(new TextWatcher() {
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

    public void updateRecordMode() {
        if (activity != null && messageInput != null && permissionsGranted) {
            boolean keyboardVisible = KeyboardVisibilityEvent.INSTANCE.isKeyboardVisible(activity);
            boolean isEmpty = messageInput.getInputEditText().getText().toString().isEmpty();
            if (keyboardVisible || !isEmpty || replyViewShowing) {
                endRecordingMode();
            } else {
                startRecordingMode();
            }
        }
    }

    protected void startRecordingMode() {
        messageInput.getButton().setImageDrawable(Icons.get(activity, Icons.choose().microphone, R.color.white));
        messageInput.getButton().setEnabled(true);
        audioModeEnabled = true;
    }

    protected void endRecordingMode() {
        messageInput.getButton().setImageDrawable(sendButtonDrawable);
        messageInput.setEnabled(!messageInput.getInputEditText().getText().toString().isEmpty());
        audioModeEnabled = false;
    }

    public void showReplyView() {
        replyViewShowing = true;
        updateRecordMode();
    }

    public void hideReplyView() {
        replyViewShowing = false;
        updateRecordMode();
    }

}
