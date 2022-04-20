package sdk.chat.app.xmpp.wow;

import android.content.Context;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.core.types.MessageType;
import sdk.chat.demo.xmpp.R;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.custom.ImageMessageRegistration;

public class WowImageMessageRegistration extends ImageMessageRegistration {

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.registerContentType(
                (byte) MessageType.Image,
                WowIncomingImageMessageViewHolder.class,
                getAvatarClickPayload(context),
                R.layout.wow_view_holder_incoming_image_message,
                WowOutcomingImageMessageViewHolder.class,
                getAvatarClickPayload(context),
                R.layout.wow_view_holder_outcoming_image_message,
                ChatSDKUI.shared().getMessageRegistrationManager());
    }
}
