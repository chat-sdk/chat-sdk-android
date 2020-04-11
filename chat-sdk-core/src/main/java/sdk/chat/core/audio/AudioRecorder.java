package sdk.chat.core.audio;

import android.media.MediaRecorder;

import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.FileManager;


/**
 * Created by ben on 9/28/17.
 */

public class AudioRecorder {

    private static final AudioRecorder instance = new AudioRecorder();

    public static AudioRecorder shared () {
        return instance;
    }

    private MediaRecorder recorder;
    private long startTime = 0;

    public File record(String name) {

        stopRecording();

        FileManager fm = ChatSDK.shared().fileManager();
        File audioFile = fm.newFile(fm.audioStorage(), name);

//        String path;
//        if (Build.VERSION.SDK_INT >= 19) {
//            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
//        }else{
//            path = new File(Environment.getExternalStorageDirectory() + "/Documents").getAbsolutePath();
//        }
//        path += AudioMessageDirectory + File.separator;
//
//        File file = new File(path);
//        file.mkdir();
//
//        path += name;
//
        Logger.debug("Recording to: " + audioFile.getPath());

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(audioFile.getPath());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
        } catch (IOException e) {
            ChatSDK.events().onError(e);
        }

        recorder.start();
        startTime = System.currentTimeMillis();

        return audioFile;
    }

    public int stopRecording() {
        long duration = duration();
        if(recorder != null) {
            try {
                recorder.stop();
            }
            catch (IllegalStateException e) {
                ChatSDK.events().onError(e);
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
