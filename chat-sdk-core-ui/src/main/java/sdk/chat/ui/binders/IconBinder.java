package sdk.chat.ui.binders;

import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.commons.ImageLoader;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.utils.ImageLoaderPayload;

public class IconBinder {
    public void bind(ImageView messageIcon, MessageHolder message, ImageLoader imageLoader) {
        if (messageIcon != null && imageLoader != null && message.getIcon() != null) {
            messageIcon.setVisibility(View.VISIBLE);
            imageLoader.loadImage(messageIcon, message.getIcon(), new ImageLoaderPayload(R.drawable.icn_200_image_message_placeholder));
        } else if (messageIcon != null) {
            messageIcon.setVisibility(View.GONE);
        }
    }
}
