package sdk.chat.custom_message.audio;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import org.pmw.tinylog.Logger;

import co.chatsdk.android.app.R;
import co.chatsdk.android.app.databinding.ViewAudioPlayerBinding;
import co.chatsdk.core.audio.AudioPlayer;
import co.chatsdk.core.utils.DisposableMap;
import co.chatsdk.ui.icons.Icons;
import io.reactivex.functions.Consumer;

/**
 * Created by ben on 9/28/17.
 */

public class AudioPlayerView extends LinearLayout {

    protected AudioPlayer player;
//    private String source;
    protected ViewAudioPlayerBinding b;
    protected DisposableMap dm = new DisposableMap();

    public AudioPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AudioPlayerView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        b = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.view_audio_player, this, true);
    }

    public void bind(String source) throws Exception {
        if (player == null) {
            player = new AudioPlayer(source, mp -> stop());

            b.playButton.setOnClickListener(view -> {
                if(player.isPlaying()) {
                    pause();
                }
                else {
                    play();
                }
            });

            b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (player != null) {
                        player.setPosition(progress);
                        updateTime();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
//                player.setPosition(seekBar.getProgress());
//                updateTime();
                }
            });

            dm.add(player.getTimeObservable().subscribe(integer -> {
                b.seekBar.setProgress(integer);
                updateTime();
            }));

        } else {
            player.setSource(source);
        }

        updatePlayPauseButton();
        updateTime();
    }

    public void updatePlayPauseButton () {
        if(player.isPlaying()) {
            b.playButton.setImageDrawable(Icons.get(Icons.choose().pause, R.color.gray_light));
        }
        else {
            b.playButton.setImageDrawable(Icons.get(Icons.choose().play, R.color.gray_light));
        }
    }

    public void play () {
        player.play();
        b.seekBar.setMax(player.durationMillis());
        b.currentTimeTextView.setText(player.position());
        b.totalTimeTextView.setText(player.duration());
        updatePlayPauseButton();
   }

    public void stop () {
        player.stop();
        b.seekBar.setProgress(0);
        updatePlayPauseButton();
    }

    public void pause () {
        player.pause();
    }

    public void updateTime () {
        setTotalTime(player.duration());
        setCurrentTime(player.position());
    }

    public void setTotalTime (String totalTime) {
        b.totalTimeTextView.setText(totalTime);
    }

    public void setCurrentTime (String currentTime) {
        b.currentTimeTextView.setText(currentTime);
    }

    public View getView () {
        return b.getRoot();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        player.dispose();
    }

}
