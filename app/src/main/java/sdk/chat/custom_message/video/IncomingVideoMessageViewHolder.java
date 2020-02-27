package sdk.chat.custom_message.video;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.android.app.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.NameBinder;
import co.chatsdk.ui.binders.OnlineStatusBinder;
import co.chatsdk.ui.icons.Icons;

public class IncomingVideoMessageViewHolder extends MessageHolders.BaseIncomingMessageViewHolder<VideoMessageHolder> {

    @BindView(R.id.playImageView) protected ImageView playImageView;
    @BindView(R.id.image) protected ImageView image;
    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.userName) protected TextView userName;
    @BindView(R2.id.imageOverlay) protected ImageView imageOverlay;

    public IncomingVideoMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        ButterKnife.bind(this, itemView);
        playImageView.setImageDrawable(Icons.getLarge(Icons.choose().play, R.color.white));
        imageOverlay.setImageDrawable(Icons.get(Icons.choose().check, R.color.white));
    }

    @Override
    public void onBind(VideoMessageHolder message) {
        super.onBind(message);

        if (image != null && imageLoader != null) {
            imageLoader.loadImage(image, message.getImageUrl(), getPayloadForImageLoader(message));
        }

        if (imageOverlay != null) {
            imageOverlay.setSelected(isSelected());
        }

        boolean isOnline = message.getUser().isOnline();
        OnlineStatusBinder.bind(onlineIndicator, isOnline);
        NameBinder.bind(userName, message);

    }

    protected Object getPayloadForImageLoader(VideoMessageHolder message) {
        return message;
    }
}
