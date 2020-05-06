package co.chatsdk.message.sticker.module;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.core.dao.Message;
import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.module.Module;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import co.chatsdk.message.sticker.R;
import co.chatsdk.message.sticker.integration.StickerMessageHolder;
import co.chatsdk.message.sticker.integration.BaseStickerMessageHandler;
import co.chatsdk.message.sticker.integration.IncomingStickerMessageViewHolder;
import co.chatsdk.message.sticker.integration.OutcomingStickerMessageViewHolder;
import co.chatsdk.message.sticker.integration.StickerChatOption;
import co.chatsdk.ui.chat.model.MessageHolder;
import co.chatsdk.ui.custom.Customiser;
import co.chatsdk.ui.custom.IMessageHandler;

/**
 * Created by ben on 10/11/17.
 */

// https://openmoji.org/
public class StickerMessageModule extends AbstractModule {

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
                        R.layout.view_holder_incoming_image_message,
                        OutcomingStickerMessageViewHolder.class,
                        R.layout.view_holder_outcoming_image_message,
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

    @Override
    public MessageHandler getMessageHandler() {
        return ChatSDK.stickerMessage();
    }

}
