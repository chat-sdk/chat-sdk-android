package co.chatsdk.core.audio;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;

/**
 * Created by ben on 9/28/17.
 */

public class Recording {

    private String name;
    public static String AudioMessagePrefix = "Audio_";
    private File file;
    private boolean isRecording;
    private Disposable delayStartDisposable;

    private int durationMillis;

    public String getMimeType() {
        return mimeType;
    }

    public int getDurationMillis() {
        return durationMillis;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    private String mimeType;

    public Recording () {
        name = AudioMessagePrefix + UUID.randomUUID() + ".m4a";
        mimeType = "audio/mp4";
    }

    // When we start, we need to start a timer to check that the user doesn't stop the recording too quickly
    public Completable start () {
        return Completable.create(e -> {
            Completable timer = Completable.timer(200, TimeUnit.MILLISECONDS);
            delayStartDisposable = timer.subscribe(() -> {
                startRecording();
                e.onComplete();
            });
        });
    }

    private void startRecording () {
        delayStartDisposable = null;
        file = AudioRecorder.shared().record(name);
    }

    public void stop () {
        if(delayStartDisposable != null) {
            delayStartDisposable.dispose();
        }
        else {
            if(AudioRecorder.shared().duration() < 1000) {
                // Wait and then stop the recording
                Completable.timer(200, TimeUnit.MILLISECONDS).subscribe(() -> AudioRecorder.shared().stopRecording());
            }
            else {
                AudioRecorder.shared().stopRecording();
                durationMillis = AudioRecorder.shared().stopRecording();
            }
        }
    }

    public String getName () {
        return name;
    }

    public void setFile (File file) {
        this.file = file;
    }

    public File getFile () {
        return file;
    }

    public void delete () {
        file.delete();
    }

}
