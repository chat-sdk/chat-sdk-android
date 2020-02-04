package co.chatsdk.core.audio;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;

import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;

import co.chatsdk.core.session.ChatSDK;


/**
 * Created by ben on 9/28/17.
 */

public class AudioRecorder {

    public static String AudioMessageDirectory = "AudioMessages" + File.separator;

    private static final AudioRecorder instance = new AudioRecorder();

    public static AudioRecorder shared () {
        return instance;
    }

    private MediaRecorder recorder;
    private long startTime = 0;

    public File record(String name) {

        stopRecording();

        String path;
        if (Build.VERSION.SDK_INT >= 19) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        }else{
            path = new File(Environment.getExternalStorageDirectory() + "/Documents").getAbsolutePath();
        }
        path += AudioMessageDirectory + File.separator;

        File file = new File(path);
        file.mkdir();

        path += name;

        Logger.debug("Recording to: " + path);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(path);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
        } catch (IOException e) {
            ChatSDK.logError(e);
        }

        recorder.start();
        startTime = System.currentTimeMillis();

        return new File (path);
    }

    public int stopRecording() {
        long duration = duration();
        if(recorder != null) {
            try {
                recorder.stop();
            }
            catch (IllegalStateException e) {
                ChatSDK.logError(e);
            }
            recorder.release();
            recorder = null;
        }
        return (int) duration;
    }

    public long duration () {
        return System.currentTimeMillis() - startTime;
    }

}
