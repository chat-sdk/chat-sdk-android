package sdk.chat.message.sticker;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.core.types.MessageType;
import sdk.chat.message.sticker.integration.IncomingStickerMessageViewHolder;
import sdk.chat.message.sticker.integration.OutcomingStickerMessageViewHolder;
import sdk.chat.message.sticker.integration.StickerMessageHolder;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.custom.DefaultMessageRegistration;

public class StickerMessageRegistration extends DefaultMessageRegistration {

    @Override
    public List<Byte> getTypes() {
        return types(MessageType.Sticker);
    }

    @Override
    public boolean hasContentFor(MessageHolder holder) {
        return holder.getClass().equals(StickerMessageHolder.class);
    }

    @Override
    public void onBindMessageHolders(Context ctx, MessageHolders holders) {
        holders.registerContentType(
                (byte) MessageType.Sticker,
                IncomingStickerMessageViewHolder.class,
                R.layout.view_holder_incoming_image_message,
                OutcomingStickerMessageViewHolder.class,
                R.layout.view_holder_outcoming_image_message,
                ChatSDKUI.shared().getMessageRegistrationManager());
    }

    @Override
    public MessageHolder onNewMessageHolder(Message message) {
        if (message.getMessageType().is(MessageType.Sticker)) {
            return new StickerMessageHolder(message);
        }
        return null;
    }

    @Override
    public boolean onClick(Activity activity, View rootView, Message message) {
        return super.onClick(activity, rootView, message);
    }

    @Override
    public boolean onLongClick(Activity activity, View rootView, Message message) {
        return false;
    }
}
