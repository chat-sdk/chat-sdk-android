package sdk.chat.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import sdk.chat.core.manager.DownloadablePayload;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.TransferStatus;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.module.UIModule;

public class ProgressView extends ConstraintLayout {

    protected CircularProgressBar circularProgressBar;
    protected TextView progressText;
    protected ImageButton actionButton;
    protected @ColorInt int tint;
    protected @ColorInt int bubbleColor;
    protected Drawable downloadDrawable;

    public ProgressView(@NonNull Context context) {
        super(context);
        this.initViews();
    }

    public ProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initViews();
    }

    public ProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initViews();
    }

    public ProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initViews();
    }

    protected void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_progress, this);

        circularProgressBar = findViewById(R.id.circularProgressBar);
        progressText = findViewById(R.id.progressText);
        actionButton = findViewById(R.id.actionButton);

        showProgressText(true);

    }

    public boolean bindSendStatus(MessageSendStatus status, MessagePayload payload) {
        DownloadablePayload dp = null;
        if (payload instanceof DownloadablePayload) {
            dp = (DownloadablePayload) payload;
        }

        // If we are uploading or downloading currently
        if (status == MessageSendStatus.Uploading || (dp != null && dp.downloadStatus() == TransferStatus.InProgress)) {
            setVisibility(VISIBLE);
            circularProgressBar.setVisibility(VISIBLE);
            actionButton.setVisibility(INVISIBLE);
            showProgressText(true);
            return true;
        }
        else if (status == MessageSendStatus.Sent || status == MessageSendStatus.None) {
            if (dp != null) {

                // If we can download, then we are not downloading...
                if (dp.canDownload()) {
                    circularProgressBar.setVisibility(VISIBLE);
                    actionButton.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                    if (dp.size() != null) {
                        showProgressText(true);
                        float size = dp.size() / 1000f;
                        if (size < 1000) {
                            progressText.setText(String.format(ChatSDK.getString(R.string.__kb), size));
                        } else {
                            size /= 1000f;
                            progressText.setText(String.format(ChatSDK.getString(R.string.__mb), size));
                        }
                    }
                    return true;
                }
            }
        }
        circularProgressBar.setVisibility(INVISIBLE);
        showProgressText(false);
        actionButton.setVisibility(INVISIBLE);
        setVisibility(GONE);
        return false;
    }

    public void showProgressText(boolean show) {
        if (UIModule.config().showMessageProgressText) {
            progressText.setVisibility(show ? VISIBLE : GONE);
        } else {
            progressText.setVisibility(GONE);
        }
    }

    public void bindProgress(MessageHolder holder) {
        boolean transferInProgress = holder.getTransferPercentage() > 0 && holder.getTransferPercentage() < 100;
        if (transferInProgress) {
            circularProgressBar.setProgress(holder.getTransferPercentage());
            progressText.setText(String.format(ChatSDK.getString(R.string.__percent), holder.getTransferPercentage()));
            circularProgressBar.setProgressBarColor(ColorUtils.blendARGB(Color.WHITE, bubbleColor, 0.6f));

            // Hide the download button
            actionButton.setVisibility(INVISIBLE);

        } else {
            circularProgressBar.setProgressBarColor(tint);
        }
    }

    public CircularProgressBar getCircularProgressBar() {
        return circularProgressBar;
    }

    public TextView getProgressText() {
        return progressText;
    }

    public ImageButton getActionButton() {
        return actionButton;
    }

    public void setTintColor(@ColorInt int color, @ColorInt int bubbleColor) {
        color = ColorUtils.blendARGB(Color.WHITE, color, 0.8f);
        tint = color;
        this.bubbleColor = bubbleColor;

        circularProgressBar.setBackgroundColor(color);
        circularProgressBar.setProgressBarColor(color);

        progressText.setTextColor(color);

        actionButton.setColorFilter(tint, PorterDuff.Mode.MULTIPLY);
    }

    public void setDefaultActionButtonDrawable() {
        actionButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.icn_60_download));
    }

//    public @ColorInt int backgroundColor() {
//
//    }

}
