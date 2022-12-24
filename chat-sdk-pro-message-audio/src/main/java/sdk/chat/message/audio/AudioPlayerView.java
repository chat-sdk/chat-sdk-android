package sdk.chat.message.audio;

import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_READY;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;

import sdk.chat.core.audio.AudioPlayer;
import sdk.chat.ui.ChatSDKUI;
import sdk.guru.common.DisposableMap;

/**
 * Created by ben on 9/28/17.
 */

public class AudioPlayerView extends LinearLayout {

    protected ImageButton playButton;
    protected SeekBar seekBar;
    protected TextView currentTimeTextView;
    protected TextView totalTimeTextView;
    protected ConstraintLayout container;

    protected AudioPlayer player;
    protected DisposableMap dm = new DisposableMap();
    protected boolean userTracking = false;

    protected String source;
    protected String totalTime;

    @ColorInt
    public int buttonColor = 0;

    @ColorInt
    public int sliderThumbColor = 0;

    @ColorInt
    public int sliderTrackColor = 0;

    @ColorInt
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

        playButton = findViewById(R.id.playButton);
        seekBar = findViewById(R.id.seekBar);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
        container = findViewById(R.id.container);

        // We will use the current time box to display both total and current time
        totalTimeTextView.setVisibility(INVISIBLE);

        playButton.setOnClickListener(view -> {
            if (player == null) {
                if (setup()) {
                    play();
                }
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
        if (source != null && !source.equals(this.source)) {
            this.source = source;
            this.player = null;
        }
        this.totalTime = totalTime;
        updatePlayPauseButton();
        updateTime();
    }

    public boolean setup() {
        if (source != null) {
            if (player == null) {

                updatePlayPauseButton();

                player = new AudioPlayer(source, (playWhenReady, playbackState) -> {
                    if (playbackState == STATE_ENDED) {
                        stop();
                    }
                    if (playbackState == STATE_READY) {
                        updateTime();
                        updatePlayPauseButton();
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
        }

        updatePlayPauseButton();
        updateTime();

        return player != null;
    }

    public void updatePlayPauseButton() {
        if (source == null) {
            playButton.setEnabled(false);
            seekBar.setEnabled(false);
        } else {
            if (player == null) {
                playButton.setEnabled(true);
                seekBar.setEnabled(false);
            } else {
                boolean ready = player.isReady();

                playButton.setEnabled(ready);
                seekBar.setEnabled(ready);
            }
            if (player != null && player.isPlaying()) {
                playButton.setImageDrawable(pauseButtonDrawable());
            } else {
                playButton.setImageDrawable(playButtonDrawable());
            }
        }
    }

    public Drawable playButtonDrawable() {
        return ChatSDKUI.icons().getWithColor(getContext(), ChatSDKUI.icons().play, buttonColor);
    }

    public Drawable pauseButtonDrawable() {
        return ChatSDKUI.icons().getWithColor(getContext(), ChatSDKUI.icons().pause, buttonColor);
    }

    public void play() {
        player.play();
        updatePlayPauseButton();
    }

    public void stop() {
        player.stop();
        seekBar.post(() -> {
            seekBar.setProgress(0);
            updatePlayPauseButton();
        });
    }

    public void pause() {
        player.pause();
        updatePlayPauseButton();
    }

    public void updateTime() {
        if (player == null) {
            setTotalTime(totalTime);
//            setCurrentTime("0:00");
        } else {
            if (player.isReady()) {
                setTotalTime(player.duration());
                seekBar.setMax(player.durationMillis().intValue());
            }
            if (player.getCurrentPosition() == 0) {
                setTotalTime(totalTime);
            } else {
                setCurrentTime(player.position());
            }
        }

        if (sliderTrackColor != 0) {
            seekBar.getProgressDrawable().setColorFilter(sliderTrackColor, PorterDuff.Mode.SRC_ATOP);
        }

        if (sliderThumbColor != 0) {
            seekBar.getThumb().setColorFilter(sliderThumbColor, PorterDuff.Mode.SRC_ATOP);
        }

        if (textColor != 0) {
            currentTimeTextView.setTextColor(textColor);
            totalTimeTextView.setTextColor(textColor);
        }

    }

    public void setTotalTime(String totalTime) {
//        totalTimeTextView.setText(totalTime);
        currentTimeTextView.setText(totalTime);
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

    public void setEnabled(boolean enabled) {
        post(() -> {
            seekBar.setEnabled(enabled);
            if (enabled) {
                playButton.setVisibility(VISIBLE);
            } else {
                playButton.setVisibility(INVISIBLE);
            }
            updatePlayPauseButton();
        });
    }

    public void setTintColor(@ColorInt int color, @ColorInt int bubbleColor) {
        buttonColor = ColorUtils.blendARGB(Color.WHITE, color, 0.7f);
        textColor = ColorUtils.blendARGB(Color.WHITE, color, 0.8f);
        sliderThumbColor = ColorUtils.blendARGB(Color.WHITE, color, 0.6f);

        // Make the track color an 80% transparency version of the tint color
        sliderTrackColor = ColorUtils.blendARGB(Color.WHITE, bubbleColor, 0.6f);

        playButton.setImageDrawable(playButtonDrawable());

    }

}
