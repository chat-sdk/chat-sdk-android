package co.chatsdk.ui.audio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.stfalcon.chatkit.messages.MessageInput;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.pmw.tinylog.Logger;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.storage.FileManager;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.core.utils.CurrentLocale;
import co.chatsdk.core.utils.DisposableMap;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.interfaces.TextInputDelegate;
import co.chatsdk.ui.utils.ToastHelper;

import static android.app.Activity.RESULT_OK;

public class AudioBinder {

    protected boolean audioModeEnabled = false;
    protected boolean permissionsGranted = false;

    protected Activity activity;
    protected TextInputDelegate delegate;
    protected DisposableMap dm = new DisposableMap();
    protected MessageInput messageInput;
    protected EditText editText;
    protected Drawable sendButtonDrawable;

    @SuppressLint("ClickableViewAccessibility")
    public AudioBinder(Activity activity, TextInputDelegate delegate, MessageInput messageInput) {
        this.activity = activity;
        this.messageInput = messageInput;
        this.delegate = delegate;

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
                        .setChannel(AudioChannel.STEREO)
                        .setSampleRate(AudioSampleRate.HZ_48000)
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

    protected void updateRecordMode() {
        if (activity != null && editText != null && permissionsGranted) {
            boolean keyboardVisible = KeyboardVisibilityEvent.INSTANCE.isKeyboardVisible(activity);
            boolean isEmpty = editText.getText().toString().isEmpty();
            if (keyboardVisible || !isEmpty) {
                endRecordingMode();
            } else {
                startRecordingMode();
            }
        }
    }

    protected void startRecordingMode() {
        if (!audioModeEnabled) {
            sendButtonDrawable = messageInput.getButton().getDrawable();
            messageInput.getButton().setImageDrawable(Icons.get(Icons.choose().microphone, R.color.white));
            messageInput.setEnabled(true);
            audioModeEnabled = true;
        }
    }

    protected void endRecordingMode() {
        if (audioModeEnabled) {
            messageInput.getButton().setImageDrawable(sendButtonDrawable);
            messageInput.setEnabled(!messageInput.getInputEditText().getText().toString().isEmpty());
//            messageInput.setImageDrawable(Icons.get(Icons.choose().send, R.color.white));
            audioModeEnabled = false;
        }
    }

}
