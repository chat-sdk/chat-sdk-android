package sdk.chat.ui.view_holders.base;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.stfalcon.chatkit.messages.MessageHolders;



import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.Base64ImageMessageHolder;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.ImageLoaderPayload;

public class BaseOutcomingBase64ImageMessageViewHolder <T extends Base64ImageMessageHolder> extends MessageHolders.BaseOutcomingMessageViewHolder<T>  {

    protected ImageView readStatus;

    protected ImageView image;
    protected ImageView imageOverlay;
    protected LinearLayout imageOverlayContainer;

    public BaseOutcomingBase64ImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);

        readStatus = itemView.findViewById(R.id.readStatus);
        image = itemView.findViewById(R.id.image);
        imageOverlay = itemView.findViewById(R.id.imageOverlay);
        imageOverlayContainer = itemView.findViewById(R.id.imageOverlayContainer);

        
    }

    @Override
    public void onBind(T message) {
        super.onBind(message);

        image.setImageBitmap(message.image());

        imageOverlay.setImageDrawable(ChatSDKUI.icons().get(imageOverlay.getContext(), ChatSDKUI.icons().check, R.color.white));

        if (imageOverlayContainer != null) {
            imageOverlayContainer.setVisibility(isSelected() ? View.VISIBLE : View.INVISIBLE);
        }

        UIModule.shared().getReadStatusViewBinder().onBind(readStatus, message);
        UIModule.shared().getMessageBinder().onBindSendStatus(time, message);
        UIModule.shared().getTimeBinder().bind(time, message);

    }

    /**
     * Override this method to have ability to pass custom data in ImageLoader for loading image(not avatar).
     *
     * @param message Message with image
     */
    protected Object getPayloadForImageLoader(T message) {
        return new ImageLoaderPayload(message.placeholder());
    }

}