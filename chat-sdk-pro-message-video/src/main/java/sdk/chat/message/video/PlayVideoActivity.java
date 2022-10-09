package sdk.chat.message.video;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import sdk.chat.core.dao.Keys;
import sdk.chat.ui.activities.BaseActivity;

/**
 * Created by ben on 10/6/17.
 */

public class PlayVideoActivity extends BaseActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, View.OnTouchListener {

    VideoView videoView;
    RelativeLayout root;

    @Override
    protected int getLayout() {
        return R.layout.activity_video_player;
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);

        videoView = findViewById(R.id.videoView);
        root = findViewById(R.id.root);

        String videoPath = "";
        Bundle e = getIntent().getExtras();
        if (e != null) {
            videoPath = e.getString(Keys.IntentKeyFilePath);
        }

        videoView.setOnCompletionListener(this);
        videoView.setOnPreparedListener(this);
        videoView.setOnTouchListener(this);

        if (!playFile(videoPath)) return;

        videoView.start();
    }
/**/
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        setIntent(intent);

        String videoPath = "";
        Bundle e = getIntent().getExtras();
        if (e != null) {
            videoPath = e.getString(Keys.IntentKeyFilePath);
        }

        if (playFile(videoPath)) {
            videoView.start();
        } else {
            finish();
        }
//
//        String fileRes = "";
//        Bundle e = getIntent().getExtras();
//        if (e != null) {
//            fileRes = e.getString("fileRes");
//        }
//        playFile(fileRes);
    }

    private boolean playFile(String fileRes) {
        if (fileRes.equals("")) {
            stopPlaying();
            return false;
        } else {
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
