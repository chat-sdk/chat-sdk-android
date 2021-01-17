package sdk.chat.demo.examples.message;

import android.content.Context;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.List;

import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.custom.CustomMessageHandler;

public class ExampleSnapMessageHandler extends CustomMessageHandler {

    public static int SnapMessageType = 919;

    @Override
    public List<Byte> getTypes() {
        return types(SnapMessageType);
    }

    @Override
    public boolean hasContentFor(MessageHolder holder) {
        return holder.getClass().equals(SnapMessageHolder.class);
    }

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.registerContentType(
                (byte) SnapMessageType,
                IncomingSnapMessageViewHolder.class,
                sdk.chat.ui.R.layout.view_holder_incoming_image_message,
                OutcomingSnapMessageViewHolder.class,
                sdk.chat.ui.R.layout.view_holder_outcoming_image_message,
                ChatSDKUI.shared().getMessageCustomizer());
    }
}
