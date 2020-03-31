package co.chatsdk.ui.view_holders.base;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.RoundedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.NameBinder;
import co.chatsdk.ui.binders.OnlineStatusBinder;
import co.chatsdk.ui.chat.model.ImageMessageHolder;
import co.chatsdk.ui.icons.Icons;

public class BaseIncomingImageMessageViewHolder<T extends ImageMessageHolder> extends MessageHolders.BaseIncomingMessageViewHolder<T>  {

    @BindView(R2.id.image) protected ImageView image;
    @BindView(R2.id.imageOverlay) protected ImageView imageOverlay;

    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.userName) protected TextView userName;

    public BaseIncomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        ButterKnife.bind(this, itemView);
        init();
    }

    @Override
    public void onBind(T message) {
        super.onBind(message);
        if (image != null && imageLoader != null) {
            imageLoader.loadImage(image, message.getImageUrl(), getPayloadForImageLoader(message));
        }

        imageOverlay.setImageDrawable(Icons.get(Icons.choose().check, R.color.white));

        if (imageOverlay != null) {
            imageOverlay.setVisibility(isSelected() ? View.VISIBLE : View.INVISIBLE);
//            imageOverlay.setSelected(isSelected());
        }

        boolean isOnline = message.getUser().isOnline();
        OnlineStatusBinder.bind(onlineIndicator, isOnline);

        NameBinder.bind(userName, message);

    }

    /**
     * Override this method to have ability to pass custom data in ImageLoader for loading image(not avatar).
     *
     * @param message Message with image
     */
    protected Object getPayloadForImageLoader(T message) {
        return R.drawable.icn_200_image_message_placeholder;
    }

    private void init() {
        if (image instanceof RoundedImageView) {
            ((RoundedImageView) image).setCorners(
                    R.dimen.message_bubble_corners_radius,
                    R.dimen.message_bubble_corners_radius,
                    R.dimen.message_bubble_corners_radius,
                    0
            );
        }
    }

}
