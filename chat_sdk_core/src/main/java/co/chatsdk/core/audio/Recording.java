package co.chatsdk.core.audio;

import java.io.File;
import java.util.UUID;

/**
 * Created by ben on 9/28/17.
 */

public class Recording {

    private String name;
    public static String AudioMessagePrefix = "Audio_";
    private File file;


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

    public void start () {
        file = AudioRecorder.shared().record(name);
    }

    public void stop () {
        durationMillis = AudioRecorder.shared().stopRecording();
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
