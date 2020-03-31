package sdk.chat.message.video;

import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import co.chatsdk.message.video.R;
import co.chatsdk.message.video.R2;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.view_holders.base.BaseIncomingImageMessageViewHolder;

public class IncomingVideoMessageViewHolder extends BaseIncomingImageMessageViewHolder<VideoMessageHolder> {

    @BindView(R2.id.playImageView) protected ImageView playImageView;

    public IncomingVideoMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        playImageView.setImageDrawable(Icons.getLarge(Icons.choose().play, R.color.white));
    }
}
