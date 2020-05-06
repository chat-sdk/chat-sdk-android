package sdk.chat.message.video;

import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import co.chatsdk.message.video.R;
import co.chatsdk.message.video.R2;
import co.chatsdk.ui.binders.MessageBinder;
import co.chatsdk.ui.binders.ReadStatusViewBinder;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.view_holders.base.BaseOutcomingImageMessageViewHolder;
import sdk.chat.core.types.MessageSendStatus;

public class OutcomingVideoMessageViewHolder extends BaseOutcomingImageMessageViewHolder<VideoMessageHolder> {

    @BindView(R2.id.playImageView) protected ImageView playImageView;

    public OutcomingVideoMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        playImageView.setImageDrawable(Icons.getLarge(Icons.choose().play, R.color.gray_very_light));
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
