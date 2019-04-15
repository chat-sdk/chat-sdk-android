package co.chatsdk.core.audio;

import android.media.MediaPlayer;

import java.util.concurrent.TimeUnit;

import co.chatsdk.core.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ben on 9/28/17.
 */

public class AudioPlayer {

    private MediaPlayer player;
    private Disposable playingDisposable;
    private ProgressListener progressListener;
    private MediaPlayer.OnCompletionListener completionListener;
    private boolean isPaused = false;

    public void play () throws Exception {
        if(player != null) {
            isPaused = false;
            player.start();

            playingDisposable = Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.single())
                    .subscribe(aLong -> {
                        if(progressListener != null && player != null) {
                            final int pos = player.getCurrentPosition();

                            AndroidSchedulers.mainThread().scheduleDirect(() -> progressListener.update(pos));
                        }
                    }, throwable -> Timber.v(throwable.getMessage()));

            player.setOnCompletionListener(completionListener);
        }
    }

    public void setCompletionListener (MediaPlayer.OnCompletionListener listener) {
        this.completionListener = listener;
    }

    public void play (String url) throws Exception {
        setSource(url);
        play();
    }

    public void setSource (String url) throws Exception {
        stop();

        player = new MediaPlayer();
        player.setDataSource(url);
        player.prepare();

    }

    public int durationMillis () {
        if(player != null) {
            return player.getDuration();
        }
        return -1;
    }

    public String duration () {
        if(player != null) {
            return toSeconds(player.getDuration());
        }
        return "";
    }

    public boolean isPlaying () {
        if(player != null) {
            return player.isPlaying();
        }
        return false;
    }

    public String position() {
        if(player != null) {
            return toSeconds(player.getCurrentPosition());
        }
        return "";
    }

    public static String toSeconds (int millis) {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }


    public void stop () {
        if(player != null) {
            player.stop();
            player.release();
            player = null;
        }
        if(playingDisposable != null) {
            playingDisposable.dispose();
        }
        isPaused = false;
    }

    public void setPosition (final int position) {
        if(player != null) {
            Schedulers.single().scheduleDirect(() -> player.seekTo(position));
        }
    }

    public void pause () {
        if(player != null) {
            isPaused = true;
            player.pause();
        }
    }

    public boolean isPaused () {
        return isPaused;
    }

    public interface ProgressListener {
        void update (int position);
    }

    public void setProgressListener (ProgressListener listener) {
        this.progressListener = listener;
    }

}
