package co.chatsdk.ui.binders;

import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.commons.ImageLoader;

import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.model.MessageHolder;

public class IconBinder {
    public static void bind(ImageView messageIcon, ImageLoader imageLoader, MessageHolder message) {
        if (messageIcon != null && imageLoader != null && message.getIcon() != null) {
            messageIcon.setVisibility(View.VISIBLE);
            imageLoader.loadImage(messageIcon, message.getIcon(), R.drawable.icn_200_image_message_placeholder);
        } else if (messageIcon != null) {
            messageIcon.setVisibility(View.GONE);
        }
    }
}
