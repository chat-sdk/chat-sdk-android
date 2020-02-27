package sdk.chat.custom_message.video;

import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.android.app.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.MessageBinder;
import co.chatsdk.ui.binders.ReadStatusViewBinder;
import co.chatsdk.ui.icons.Icons;

public class OutcomingVideoMessageViewHolder extends MessageHolders.BaseOutcomingMessageViewHolder<VideoMessageHolder> {

    @BindView(R.id.playImageView) protected ImageView playImageView;
    @BindView(R2.id.readStatus) protected ImageView readStatus;
    @BindView(R2.id.image) protected ImageView image;

    public OutcomingVideoMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        ButterKnife.bind(this, itemView);
        playImageView.setImageDrawable(Icons.get(Icons.choose().play, R.color.white));
    }

    @Override
    public void onBind(VideoMessageHolder holder) {
        super.onBind(holder);

        ReadStatusViewBinder.onBind(readStatus, holder);
        MessageBinder.onBindSendStatus(time, holder);

        if (image != null && imageLoader != null) {
            imageLoader.loadImage(image, holder.getImageUrl(), getPayloadForImageLoader(holder));
        }

//        if (imageOverlay != null) {
//            imageOverlay.setSelected(isSelected());
//        }

    }

    protected Object getPayloadForImageLoader(VideoMessageHolder message) {
        return message;
    }

}
