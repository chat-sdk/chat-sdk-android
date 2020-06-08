package sdk.chat.message.audio;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.Player;

import org.pmw.tinylog.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.core.audio.AudioPlayer;
import sdk.chat.ui.icons.Icons;
import sdk.guru.common.DisposableMap;

import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_READY;

/**
 * Created by ben on 9/28/17.
 */

public class AudioPlayerView extends LinearLayout {

    @BindView(R2.id.playButton) ImageButton playButton;
    @BindView(R2.id.seekBar) SeekBar seekBar;
    @BindView(R2.id.currentTimeTextView) TextView currentTimeTextView;
    @BindView(R2.id.totalTimeTextView) TextView totalTimeTextView;
    @BindView(R2.id.bubble) RelativeLayout bubble;

    protected AudioPlayer player;
    protected DisposableMap dm = new DisposableMap();
    protected boolean userTracking = false;

    protected String source;
    protected String totalTime;

    @ColorRes
    public int buttonColor = R.color.gray_light;

    @ColorRes
    public int sliderThumbColor = 0;

    @ColorRes
    public int sliderTrackColor = 0;

    @ColorRes
    public int textColor = 0;

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
        LayoutInflater.from(getContext()).inflate(R.layout.view_audio_player, this);
        ButterKnife.bind(this);

        Logger.debug("New AudioPlayerView");

        playButton.setOnClickListener(view -> {
            if (player == null) {
                setup();
                play();
            } else {
                if (player.isPlaying()) {
                    pause();
                } else {
                    play();
                }
            }
        });
    }

    public void bind(String source, String totalTime) {
        if (!source.equals(this.source)) {
            this.source = source;
            this.player = null;
            this.totalTime = totalTime;
        }
        updatePlayPauseButton();
        updateTime();
    }

    public void setup() {
        if (player == null) {

            updatePlayPauseButton();

            player = new AudioPlayer(source, new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if (playbackState == STATE_ENDED) {
                        stop();
                    }
                    if (playbackState == STATE_READY) {
                        updateTime();
                        updatePlayPauseButton();
                    }
                }
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (userTracking) {
                        player.setPosition(progress);
                    }
                    updateTime();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    userTracking = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    userTracking = false;
                }
            });

            dm.add(player.getTimeObservable().subscribe(integer -> {
                seekBar.setProgress(integer.intValue());
                updateTime();
            }));

        } else {
            player.setSource(source);
        }

        updatePlayPauseButton();
        updateTime();
    }

    public void updatePlayPauseButton() {

        if (player == null) {
            playButton.setEnabled(true);
            seekBar.setEnabled(false);
        } else {
            boolean ready = player.isReady();

            playButton.setEnabled(ready);
            seekBar.setEnabled(ready);
        }
        if (player != null && player.isPlaying()) {
            playButton.setImageDrawable(Icons.get(Icons.choose().pause, buttonColor));
        } else {
            playButton.setImageDrawable(Icons.get(Icons.choose().play, buttonColor));
        }
    }

    public void play() {
        player.play();
        updatePlayPauseButton();
    }

    public void stop() {
        player.stop();
        seekBar.setProgress(0);
        updatePlayPauseButton();
    }

    public void pause() {
        player.pause();
        updatePlayPauseButton();
    }

    public void updateTime() {
        if (player == null) {
            setTotalTime(totalTime);
            setCurrentTime("0:00");
        } else {
            if (player.isReady()) {
                setTotalTime(player.duration());
                seekBar.setMax(player.durationMillis().intValue());
            }
            setCurrentTime(player.position());
        }

        if (sliderTrackColor != 0) {
            seekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(getContext(), sliderTrackColor), PorterDuff.Mode.SRC_ATOP);
        }

        if (sliderThumbColor != 0) {
            seekBar.getThumb().setColorFilter(ContextCompat.getColor(getContext(), sliderThumbColor), PorterDuff.Mode.SRC_ATOP);
        }

        if (textColor != 0) {
            currentTimeTextView.setTextColor(ContextCompat.getColor(getContext(), textColor));
            totalTimeTextView.setTextColor(ContextCompat.getColor(getContext(), textColor));
        }

    }

    public void setTotalTime(String totalTime) {
        totalTimeTextView.setText(totalTime);
    }

    public void setCurrentTime(String currentTime) {
        currentTimeTextView.setText(currentTime);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        source = null;
        if (player != null) {
            player.dispose();
        }
        player = null;
    }

}
