package co.chatsdk.core.audio;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.pmw.tinylog.Logger;

import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.CurrentLocale;
import co.chatsdk.core.utils.DisposableMap;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.google.android.exoplayer2.Player.STATE_READY;

/**
 * Created by ben on 9/28/17.
 */

public class AudioPlayer {

    protected final SimpleExoPlayer player = new SimpleExoPlayer.Builder(ChatSDK.shared().context()).build();
    protected Disposable playingDisposable;

    protected PublishSubject<Long> timePublishSubject = PublishSubject.create();
    protected boolean isReady = false;

    public AudioPlayer(String source, Player.EventListener listener) {
        setSource(source);

        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == STATE_READY) {
                    isReady = true;
                }
                listener.onPlayerStateChanged(playWhenReady, playbackState);
            }
        });

    }

    public void play () {
        player.setPlayWhenReady(true);

        playingDisposable = Observable.interval(0, 30, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (player.isPlaying()) {
                        timePublishSubject.onNext(player.getCurrentPosition());
                    }
                }, throwable -> Logger.error(throwable.getMessage()));
    }

    public void setSource(String url) {
        stop();
        isReady = false;

        Context context = ChatSDK.shared().context();

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "ChatSDK"));

        // This is the MediaSource representing the media to be played.
        MediaSource audioSource =
                new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(url));

        // Prepare the player with the source.
        player.prepare(audioSource, false, true);

    }

    public void stop() {
        pause();
        seekTo(0);
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
        return player.isPlaying();
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
        return timePublishSubject.hide().observeOn(AndroidSchedulers.mainThread());
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
