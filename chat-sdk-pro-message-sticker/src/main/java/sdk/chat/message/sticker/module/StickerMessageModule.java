package sdk.chat.message.sticker.module;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.core.dao.Message;
import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.core.types.MessageType;
import sdk.chat.message.sticker.R;
import sdk.chat.message.sticker.integration.BaseStickerMessageHandler;
import sdk.chat.message.sticker.integration.IncomingStickerMessageViewHolder;
import sdk.chat.message.sticker.integration.OutcomingStickerMessageViewHolder;
import sdk.chat.message.sticker.integration.StickerChatOption;
import sdk.chat.message.sticker.integration.StickerMessageHolder;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.custom.Customiser;
import sdk.chat.ui.custom.IMessageHandler;
import sdk.guru.common.BaseConfig;

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

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<StickerMessageModule> builder() {
        return instance.config;
    }

    public static StickerMessageModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public int maxSize = 400;

        public Config(T onBuild) {
            super(onBuild);
        }

        public Config<T> setMaxSize(int size) {
            this.maxSize = size;
            return this;
        }

    }

    public Config<StickerMessageModule> config = new Config<>(this);

    public static Config config() {
        return shared().config;
    }

    @Override
    public void stop() {
        config = new Config<>(this);
    }

}
