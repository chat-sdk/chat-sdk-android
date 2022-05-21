package sdk.chat.message;

import android.content.Context;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.android.live.R;
import sdk.chat.core.types.MessageType;
import sdk.chat.message.video.V2Video;
import sdk.chat.message.video.VideoMessageRegistration;
import sdk.chat.ui.ChatSDKUI;

public class V2VideoMessageRegistration extends VideoMessageRegistration {

    @Override
    public void onBindMessageHolders(Context ctx, MessageHolders holders) {
        holders.registerContentType(
                (byte) MessageType.Video,
                V2Video.OutcomingVideoMessageViewHolder.class,
                R.layout.view_holder_incoming_image_message,
                V2Video.IncomingVideoMessageViewHolder.class,
                R.layout.view_holder_outcoming_image_message,
                ChatSDKUI.shared().getMessageRegistrationManager());
    }

}
