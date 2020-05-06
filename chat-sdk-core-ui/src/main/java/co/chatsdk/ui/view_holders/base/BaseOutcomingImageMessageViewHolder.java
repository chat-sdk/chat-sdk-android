package co.chatsdk.ui.view_holders.base;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import co.chatsdk.ui.utils.ImageLoaderPayload;

public class BaseOutcomingImageMessageViewHolder<T extends ImageMessageHolder> extends MessageHolders.BaseOutcomingMessageViewHolder<T>  {

    @BindView(R2.id.readStatus) protected ImageView readStatus;

    @BindView(R2.id.image) protected ImageView image;
    @BindView(R2.id.imageOverlay) protected ImageView imageOverlay;
    @BindView(R2.id.imageOverlayContainer) protected LinearLayout imageOverlayContainer;

    public BaseOutcomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(T message) {
        super.onBind(message);
        if (image != null && imageLoader != null) {
            imageLoader.loadImage(image, message.getImageUrl(), getPayloadForImageLoader(message));
        }

        imageOverlay.setImageDrawable(Icons.get(Icons.choose().check, R.color.white));

        if (imageOverlayContainer != null) {
            imageOverlayContainer.setVisibility(isSelected() ? View.VISIBLE : View.INVISIBLE);
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
        return new ImageLoaderPayload(R.drawable.icn_200_image_message_placeholder);
    }

}
