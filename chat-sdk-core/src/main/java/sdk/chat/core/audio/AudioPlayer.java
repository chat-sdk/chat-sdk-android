package sdk.chat.core.audio;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.jakewharton.rxrelay2.PublishRelay;

import org.pmw.tinylog.Logger;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.CurrentLocale;
import sdk.guru.common.RX;

import static com.google.android.exoplayer2.Player.STATE_READY;

/**
 * Created by ben on 9/28/17.
 */

public class AudioPlayer {

    protected final SimpleExoPlayer player = new SimpleExoPlayer.Builder(ChatSDK.ctx()).build();
    protected Disposable playingDisposable;

    protected PublishRelay<Long> timePublishRelay = PublishRelay.create();
    protected boolean isReady = false;

    protected String mediaSource = null;

    public AudioPlayer(String source, Player.EventListener listener) {
        setSource(source);

        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Logger.debug("Playback state: " + playbackState);
                if (playbackState == STATE_READY) {
                    isReady = true;
                }
                listener.onPlayerStateChanged(playWhenReady, playbackState);
            }
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Logger.debug("Playback error: " + error);
            }
        });

    }

    public void play() {
        player.setPlayWhenReady(true);

        playingDisposable = Observable.interval(0, 30, TimeUnit.MILLISECONDS)
                .observeOn(RX.main())
                .subscribe(aLong -> {
                    if (player.isPlaying()) {
                        timePublishRelay.accept(player.getCurrentPosition());
                    }
                }, throwable -> Logger.error(throwable.getMessage()));
    }

    public void setSource(String url) {
        if (url != null && !url.equals(mediaSource)) {
            stop();
            isReady = false;

            Context context = ChatSDK.ctx();

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "ChatSDK"));

            // This is the MediaSource representing the media to be played.
            MediaSource audioSource =
                    new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(Uri.parse(url));

            // Prepare the player with the source.
            player.prepare(audioSource, true, true);
            mediaSource = url;
        }
    }

    public void stop() {
        if (isPlaying()) {
            pause();
        }
        if (player.getCurrentPosition() > 0.5) {
            seekTo(0);
        }
    }

    public void pause () {
        player.setPlayWhenReady(false);
        if (playingDisposable != null) {
            playingDisposable.dispose();
        }
    }

    public Long durationMillis () {
        return player.getDuration();
    }

    public String duration () {
//        if (player.getDuration() < 0) {
//            return null;
//        }
        return toSeconds(player.getDuration());
    }

    public boolean isPlaying () {
        return player.isPlaying() || player.getPlayWhenReady();
    }

    public String position() {
        return toSeconds(player.getCurrentPosition());
    }

    public static String toSeconds (long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long minuteSeconds = TimeUnit.MINUTES.toSeconds(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        return String.format(CurrentLocale.get(), "%d:%02d", minutes, seconds - minuteSeconds);
    }

    public void setPosition (final int position) {
        seekTo(position);
    }

    public void seekTo(long ms) {
        player.seekTo(ms);
    }

    public Observable<Long> getTimeObservable() {
        return timePublishRelay.hide().observeOn(RX.main());
    }

    public void dispose() {
        if (playingDisposable != null) {
            playingDisposable.dispose();
        }
        stop();
        player.release();
    }

    public boolean isReady() {
        return isReady;
    }

}
