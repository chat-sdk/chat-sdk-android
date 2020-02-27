package sdk.chat.custom_message.video;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

import androidx.databinding.DataBindingUtil;

import co.chatsdk.android.app.R;
import co.chatsdk.android.app.databinding.ActivityVideoPlayerBinding;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.ui.activities.BaseActivity;

/**
 * Created by ben on 10/6/17.
 */

public class PlayVideoActivity extends BaseActivity implements MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener,View.OnTouchListener {

    private VideoView videoView;

    protected ActivityVideoPlayerBinding b;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        b = DataBindingUtil.setContentView(this, getLayout());

        String videoPath = "";
        Bundle e = getIntent().getExtras();
        if (e!=null) {
            videoPath = e.getString(Keys.IntentKeyFilePath);
        }

        b.videoView.setOnCompletionListener(this);
        b.videoView.setOnPreparedListener(this);
        b.videoView.setOnTouchListener(this);

        if (!playFile(videoPath)) return;

        b.videoView.start();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_video_player;
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String fileRes = "";
        Bundle e = getIntent().getExtras();
        if (e != null) {
            fileRes = e.getString("fileRes");
        }
        playFile(fileRes);
    }

    private boolean playFile(String fileRes) {
        if (fileRes.equals("")) {
            stopPlaying();
            return false;
        }
        else {
            videoView.setVideoURI(Uri.parse(fileRes));
            return true;
        }
    }

    public void stopPlaying() {
        videoView.stopPlayback();
        this.finish();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        finish();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        stopPlaying();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setLooping(true);
    }
}
