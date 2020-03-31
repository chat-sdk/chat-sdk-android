package co.chatsdk.ui.view_holders.base;

import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.RoundedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.MessageBinder;
import co.chatsdk.ui.binders.ReadStatusViewBinder;
import co.chatsdk.ui.chat.model.ImageMessageHolder;
import co.chatsdk.ui.icons.Icons;

public class BaseOutcomingImageMessageViewHolder<T extends ImageMessageHolder> extends MessageHolders.BaseOutcomingMessageViewHolder<T>  {

    @BindView(R2.id.readStatus) protected ImageView readStatus;

    @BindView(R2.id.image) protected ImageView image;
    @BindView(R2.id.imageOverlay) protected ImageView imageOverlay;

    public BaseOutcomingImageMessageViewHolder(View itemView, Object payload) {
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
        }

        ReadStatusViewBinder.onBind(readStatus, message);
        MessageBinder.onBindSendStatus(time, message);

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
                    0,
                    R.dimen.message_bubble_corners_radius
            );
        }
    }

}
