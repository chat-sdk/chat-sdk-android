package co.chatsdk.core.audio;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import com.google.android.gms.drive.events.CompletionListener;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ben on 9/28/17.
 */

public class AudioPlayer {

    private MediaPlayer player;
    private Disposable playingDisposable;
    private ProgressListener progressListener;
    private MediaPlayer.OnCompletionListener completionListener;

    public void play () throws Exception {
        if(player != null) {
            player.start();

            playingDisposable = Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(@NonNull Long aLong) throws Exception {
                            if(progressListener != null) {
                                progressListener.update(player.getCurrentPosition(), player.getDuration());
                            }
                        }
                    });

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
        return String.format("%d:%d00.",
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
    }

    public void setPosition (int position) {
        if(player != null) {
            player.seekTo(position);
        }
    }

    public void pause () {
        if(player != null) {
            player.pause();
        }
    }

    public interface ProgressListener {
        void update (int elapsed, int duration);
    }

    public void setProgressListener (ProgressListener listener) {
        this.progressListener = listener;
    }

}
