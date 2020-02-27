package co.chatsdk.ui.audio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import co.chatsdk.core.audio.Recording;
import co.chatsdk.core.session.ChatSDK;
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

public class AudioBinder {

    protected boolean audioModeEnabled = false;
    protected boolean permissionsGranted = false;

    protected Recording recording = null;
    protected InfiniteToast toast;
    protected Rect rect;
    protected Date recordingStart;
//    protected boolean audioMaxLengthReached = false;
//    protected Disposable toastUpdateDisposable;

    protected Activity activity;
    protected TextInputDelegate delegate;
    protected DisposableMap dm = new DisposableMap();
    protected ImageButton sendButton;
    protected EditText input;

    @SuppressLint("ClickableViewAccessibility")
    public AudioBinder(Activity activity, ImageButton sendButton, EditText input) {
        this.activity = activity;
        this.sendButton = sendButton;
        this.input = input;

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

        sendButton.setOnTouchListener((view, motionEvent) ->  {
            if(audioModeEnabled) {
                    // Start recording when we press down
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        startRecording(view);
                    }
                    // Stop recording
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        stopRecording(view, motionEvent);
                    }
                    // Show a toast if they move out of the recording box
                    if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                        if (recording != null) {
                            toast.cancel();
                            if (inRect(view, motionEvent)) {
                                toast = new InfiniteToast(activity, R.string.recording, false);
                            } else {
                                toast = new InfiniteToast(activity, R.string.recording, false);
                            }
                        }
                    }
            }
            return sendButton.onTouchEvent(motionEvent);
        });

        updateRecordMode();
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
            sendButton.setImageDrawable(Icons.get(Icons.choose().microphone, R.color.white));
            sendButton.setEnabled(true);
            audioModeEnabled = true;
        }
    }

    protected void endRecordingMode() {
        if (audioModeEnabled) {
            sendButton.setImageDrawable(Icons.get(Icons.choose().send, R.color.white));
            audioModeEnabled = false;
        }
    }

    public void startRecording (View view) {

//        audioMaxLengthReached = false;

        rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        recording = new Recording();
        recordingStart = new Date();

//        dm.add(recording.start().subscribe(() -> {
//            toast = new InfiniteToast(activity, R.string.recording, false);
//        }, ChatSDK.events()));

        dm.add(recording.start().subscribe(() -> {}, ChatSDK.events()));


//        toastUpdateDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(aLong -> {
//            long remainingTime = ChatSDK.config().audioMessageMaxLengthSeconds - (new Date().getSeconds() - recordingStart.getSeconds());
//            if (remainingTime <= 10) {
//                toast.setText(String.format(activity.getString(R.string.seconds_remaining__), remainingTime));
//            }
//            if (remainingTime <= 0) {
//                audioMaxLengthReached = true;
//                this.presentAlertView();
//            }
//        }, ChatSDK.events());

    }

//    public void presentAlertView () {
//        finishRecording();
//        toast.hide();
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setTitle(activity.getString(R.string.audio_length_limit_reached));
//
//        // Set up the buttons
//        builder.setPositiveButton(activity.getString(R.string.send), (dialog, which) -> {
//            delegate.sendAudio(recording);
//            recording = null;
//            dialog.cancel();
//            audioMaxLengthReached = false;
//        });
//        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
//            dialog.cancel();
//            audioMaxLengthReached = false;
//        });
//
//        builder.show();
//
//    }

    public void stopRecording (View view, MotionEvent motionEvent) {

//        if (audioMaxLengthReached) {
//            return;
//        }

        finishRecording();
        if(recording != null) {
            if(delegate != null && recording.getDurationMillis() > 1000) {
                if(!inRect(view, motionEvent)){
                    // User moved outside bounds
                    ToastHelper.show(activity, activity.getString(R.string.recording_cancelled));
                }
                else {
                    delegate.sendAudio(recording);
                }
            }
            else {
                ToastHelper.show(activity, activity.getString(R.string.recording_too_short));
            }
            recording = null;
        }
    }

    public boolean inRect(View view, MotionEvent motionEvent) {
        return rect.contains(view.getLeft() + (int) motionEvent.getX(), view.getTop() + (int) motionEvent.getY());
    }

    public void finishRecording () {
        if(recording != null) {
            recording.stop();
        }
        if(toast != null) {
            toast.cancel();
        }

//        if (toastUpdateDisposable != null) {
//            toastUpdateDisposable.dispose();
//        }
    }

}
