package sdk.chat.custom_message.audio;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import co.chatsdk.android.app.R;
import co.chatsdk.android.app.databinding.ViewAudioPlayerBinding;
import co.chatsdk.core.audio.AudioPlayer;
import co.chatsdk.ui.icons.Icons;

/**
 * Created by ben on 9/28/17.
 */

public class AudioPlayerView extends LinearLayout {

    private AudioPlayer player = new AudioPlayer();
    private String source;
    protected ViewAudioPlayerBinding b;

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

        b.totalTimeTextView.setText("");

        b.playButton.setOnClickListener(view -> {
            if(player.isPlaying()) {
                pause();
            }
            else {
                play();
            }
            updatePlayPauseButton();
        });

        player.setCompletionListener(mediaPlayer -> stop());

        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(player != null) {
                    player.setPosition(seekBar.getProgress());
                    updateTime();
                }
            }
        });

        updatePlayPauseButton();

    }

    public void updatePlayPauseButton () {
        if(player.isPlaying()) {
            b.playButton.setImageDrawable(Icons.get(Icons.choose().pause, R.color.gray_light));
        }
        else {
            b.playButton.setImageDrawable(Icons.get(Icons.choose().play, R.color.gray_light));
        }
    }

    public void setSource (String url) throws Exception {
        source = url;
    }

    public void play () {
        try {

            if(!player.isPaused()) {
                player.setSource(source);
                b.seekBar.setMax(player.durationMillis());
                b.currentTimeTextView.setText(player.position());
                b.totalTimeTextView.setText(player.duration());
            }

            player.play();

            player.setProgressListener(elapsed -> {
                b.seekBar.setProgress(elapsed);
                updateTime();
            });
        }
        catch (Exception e) {
            // TODO: Handle this
        }
    }

    public void stop () {
        player.stop();
        b.seekBar.setProgress(0);
        updatePlayPauseButton();
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

    public void pause () {
        player.pause();
    }

    public View getView () {
        return b.getRoot();
    }

    public void setBackgroundColor (int color) {
        Drawable d = getResources().getDrawable(R.drawable.layout_bg);
        d.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
//        bubbleLayout.setBackground(d);
    }

}
