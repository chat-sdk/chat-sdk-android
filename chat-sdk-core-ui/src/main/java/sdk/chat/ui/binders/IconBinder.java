package sdk.chat.ui.binders;

import android.view.View;
import android.widget.ImageView;

import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.chat.model.MessageHolder;

public class IconBinder {
    public void bind(ImageView messageIcon, MessageHolder message) {
        if (messageIcon != null && message.getIcon() != null) {
            messageIcon.setVisibility(View.VISIBLE);
            ChatSDKUI.provider().imageLoader().loadIcon(messageIcon, message.getIcon());
        } else if (messageIcon != null) {
            messageIcon.setVisibility(View.GONE);
        }
    }
}
