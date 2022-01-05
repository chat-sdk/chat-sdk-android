package sdk.chat.message.video;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import butterknife.BindView;
import sdk.chat.core.dao.Keys;
import sdk.chat.ui.activities.BaseActivity;

public class ExoVideoActivity extends BaseActivity {

    @BindView(R2.id.videoPlayerView) PlayerView playerView;

    protected SimpleExoPlayer player;

    protected boolean playWhenReady = true;
    protected int currentWindow = 0;
    protected long playbackPosition = 0L;

//    protected Cache simpleCache;
//    protected DatabaseProvider databaseProvider;
//    protected CacheEvictor evictor;

    @Override
    protected int getLayout() {
        return R.layout.activity_exo_video_player;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        databaseProvider = new ExoDatabaseProvider(this);
//        evictor = new LeastRecentlyUsedCacheEvictor(VideoMessageModule.shared().config.cacheSizeMB * 1024 * 1024);
//
//        File cache = ChatSDK.shared().fileManager().videoStorage();
//        simpleCache = new SimpleCache(cache, evictor, databaseProvider);


        if (getVideoPath() == null) {
            finish();
        }

        initializePlayer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || player == null) {
            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            releasePlayer();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            releasePlayer();
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (getVideoPath() == null) {
            finish();
        }
    }

    protected void addItem(String url) {
        MediaItem item = MediaItem.fromUri(url);
        player.setMediaItem(item);
    }

    protected String getVideoPath() {
        String videoPath = null;
        Bundle e = getIntent().getExtras();
        if (e != null) {
            videoPath = e.getString(Keys.IntentKeyFilePath);
        }
        return videoPath;
    }

    protected void initializePlayer() {
        String url = getVideoPath();
        if (player == null && url != null) {

            player = new SimpleExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            MediaItem item = MediaItem.fromUri(url);

//            CacheDataSource.Factory dataSource = new CacheDataSource.Factory().setCache(simpleCache);
//            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSource).createMediaSource(item);
//            player.setMediaSource(mediaSource);
            player.addMediaItem(item);

            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
            player.prepare();
        }
    }

    protected void hideSystemUI() {
//        playerView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LOW_PROFILE |
//                    View.SYSTEM_UI_FLAG_FULLSCREEN |
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//        );
    }

    protected void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }
}
