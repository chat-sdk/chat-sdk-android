package sdk.chat.message.video;

import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.view_holders.base.BaseOutcomingImageMessageViewHolder;

public class OutcomingVideoMessageViewHolder extends BaseOutcomingImageMessageViewHolder<VideoMessageHolder> {

    @BindView(R2.id.playImageView) protected ImageView playImageView;

    public OutcomingVideoMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        playImageView.setImageDrawable(Icons.getLarge(ChatSDKUI.icons().play, R.color.gray_very_light));
    }

    @Override
    public void onBind(VideoMessageHolder message) {
        super.onBind(message);
        if (message.getMessage().getMessageStatus().equals(MessageSendStatus.Sent)) {
            playImageView.setVisibility(View.VISIBLE);
        } else {
            playImageView.setVisibility(View.INVISIBLE);
        }
    }
}
