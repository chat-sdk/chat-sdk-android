package sdk.chat.ui.view_holders.base;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.binders.MessageBinder;
import sdk.chat.ui.binders.ReadStatusViewBinder;
import sdk.chat.ui.chat.model.ImageMessageHolder;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.ImageLoaderPayload;

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
            String url = message.getImageUrl();
            imageLoader.loadImage(image, url, getPayloadForImageLoader(message));
        }

        imageOverlay.setImageDrawable(Icons.get(imageOverlay.getContext(), Icons.choose().check, R.color.white));

        if (imageOverlayContainer != null) {
            imageOverlayContainer.setVisibility(isSelected() ? View.VISIBLE : View.INVISIBLE);
        }

        UIModule.shared().getReadStatusViewBinder().onBind(readStatus, message);
        UIModule.shared().getMessageBinder().onBindSendStatus(time, message);

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
