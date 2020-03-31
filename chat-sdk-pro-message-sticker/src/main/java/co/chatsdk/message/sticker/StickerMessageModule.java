package co.chatsdk.message.sticker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import org.pmw.tinylog.Logger;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;

import co.chatsdk.core.types.MessageType;
import co.chatsdk.ui.chat.model.MessageHolder;
import co.chatsdk.ui.custom.Customiser;
import co.chatsdk.ui.custom.IMessageHandler;

/**
 * Created by ben on 10/11/17.
 */

public class StickerMessageModule implements Module {

    public static final int CHOOSE_STICKER = 105;

    public static final StickerMessageModule instance = new StickerMessageModule();

    public static StickerMessageModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        ChatSDK.a().stickerMessage = new BaseStickerMessageHandler();
        ChatSDK.ui().addChatOption(new StickerChatOption(context.getResources().getString(R.string.sticker_message)));

        Customiser.shared().addMessageHandler(new IMessageHandler() {
            @Override
            public boolean hasContentFor(MessageHolder message, byte type) {
                return type == MessageType.Sticker && message instanceof StickerMessageHolder;
            }

            @Override
            public void onBindMessageHolders(Context ctx, MessageHolders holders) {
                holders.registerContentType(
                        (byte) MessageType.Sticker,
                        IncomingStickerMessageViewHolder.class,
                        R.layout.view_holder_incoming_text_message,
                        OutcomingStickerMessageViewHolder.class,
                        R.layout.view_holder_outcoming_text_message,
                        this);
            }

            @Override
            public MessageHolder onNewMessageHolder(Message message) {
                if (message.getMessageType().is(MessageType.Sticker)) {
                    return new StickerMessageHolder(message);
                }
                return null;
            }

            @Override
            public void onClick(Activity activity, View rootView, Message message) {

            }

            @Override
            public void onLongClick(Activity activity, View rootView, Message message) {

            }
        });
    }

    @Override
    public String getName() {
        return "StickerMessagesModule";
    }
}
