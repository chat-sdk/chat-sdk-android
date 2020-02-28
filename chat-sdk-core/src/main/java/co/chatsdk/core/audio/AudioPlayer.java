package co.chatsdk.core.audio;

import android.media.MediaPlayer;

import org.pmw.tinylog.Logger;

import java.util.concurrent.TimeUnit;

import co.chatsdk.core.utils.DisposableMap;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by ben on 9/28/17.
 */

public class AudioPlayer {

    protected final MediaPlayer player = new MediaPlayer();
    protected Disposable playingDisposable;

    protected PublishSubject<Integer> timePublishSubject = PublishSubject.create();

    public AudioPlayer(String source, MediaPlayer.OnCompletionListener completionListener) throws Exception {
        setSource(source);
        player.setOnCompletionListener(completionListener);
        playingDisposable = Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.single())
                .subscribe(aLong -> {
                    if (player.isPlaying()) {
                        timePublishSubject.onNext(player.getCurrentPosition());
                    }
                }, throwable -> Logger.error(throwable.getMessage()));
        player.setOnErrorListener((mp, what, extra) -> {
            Logger.debug("Error");
            return false;
        });
    }

    public void play () {
        player.start();
    }

    public void setSource(String url) throws Exception {
        stop();
        player.setDataSource(url);
        player.prepare();
    }

    public void stop() {
        player.stop();
    }

    public void pause () {
        player.pause();
    }

    public int durationMillis () {
        return player.getDuration();
    }

    public String duration () {
        return toSeconds(player.getDuration());
    }

    public boolean isPlaying () {
        return player.isPlaying();
    }

    public String position() {
        return toSeconds(player.getCurrentPosition());
    }

    public static String toSeconds (int millis) {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public void setPosition (final int position) {
        Schedulers.single().scheduleDirect(() -> player.seekTo(position));
    }

    public boolean isPaused () {
        return !player.isPlaying();
    }

    public Observable<Integer> getTimeObservable() {
        return timePublishSubject.hide();
    }

    public void dispose() {
        playingDisposable.dispose();
        stop();
        player.release();
    }

}
