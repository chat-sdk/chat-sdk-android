package sdk.chat.message.video;

import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.view_holders.base.BaseIncomingImageMessageViewHolder;

public class IncomingVideoMessageViewHolder extends BaseIncomingImageMessageViewHolder<VideoMessageHolder> {

    @BindView(R2.id.playImageView) protected ImageView playImageView;

    public IncomingVideoMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        playImageView.setImageDrawable(Icons.getLarge(ChatSDKUI.icons().play, R.color.gray_very_light));
    }
}
