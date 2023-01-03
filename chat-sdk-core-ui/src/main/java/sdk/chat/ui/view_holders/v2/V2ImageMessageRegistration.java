package sdk.chat.ui.view_holders.v2;

import android.content.Context;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.ui.custom.ImageMessageRegistration;
import sdk.chat.ui.view_holders.v2.outer.V2;

@Deprecated
public class V2ImageMessageRegistration  extends ImageMessageRegistration {

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.setIncomingImageConfig(V2.IncomingImageMessageViewHolder.class, sdk.chat.ui.R.layout.view_holder_incoming_image_message)
                .setOutcomingImageConfig(V2.OutcomingImageMessageViewHolder.class, sdk.chat.ui.R.layout.view_holder_outcoming_image_message);
    }

}