package sdk.chat.sinch.calling;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

import sdk.chat.sinch.R;

public class CarAudioPlayer {

    private static final String LOG_TAG = CarAudioPlayer.class.getSimpleName();

    private Context mContext;
    private MediaPlayer mPlayer;
    private AudioTrack mProgressTone;

    private final static int SAMPLE_RATE = 16000;

    CarAudioPlayer(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void playRingtone() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        // Honour silent mode
        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:
                mPlayer = new MediaPlayer();
                mPlayer.setAudioStreamType(AudioManager.STREAM_RING);

                try {
                    mPlayer.setDataSource(mContext,
                            Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.phone_loud1));
                    mPlayer.prepare();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Could not setup media player for ringtone");
                    mPlayer = null;
                    return;
                }
                mPlayer.setLooping(true);
                mPlayer.start();
                break;
        }
    }

    public void stopRingtone() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void playProgressTone() {
        stopProgressTone();
        try {
            mProgressTone = createProgressTone(mContext);
            mProgressTone.play();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not play progress tone", e);
        }
    }

    public void stopProgressTone() {
        if (mProgressTone != null) {
            mProgressTone.stop();
            mProgressTone.release();
            mProgressTone = null;
        }
    }

    private static AudioTrack createProgressTone(Context context) throws IOException {
        AssetFileDescriptor fd = context.getResources().openRawResourceFd(R.raw.progress_tone);
        int length = (int) fd.getLength();

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, length, AudioTrack.MODE_STATIC);

        byte[] data = new byte[length];
        readFileToBytes(fd, data);

        audioTrack.write(data, 0, data.length);
        audioTrack.setLoopPoints(0, data.length / 2, 30);

        return audioTrack;
    }

    private static void readFileToBytes(AssetFileDescriptor fd, byte[] data) throws IOException {
        FileInputStream inputStream = fd.createInputStream();

        int bytesRead = 0;
        while (bytesRead < data.length) {
            int res = inputStream.read(data, bytesRead, (data.length - bytesRead));
            if (res == -1) {
                break;
            }
            bytesRead += res;
        }
    }
}
