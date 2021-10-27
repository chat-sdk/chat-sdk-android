package sdk.chat.message.sticker.module;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.RawRes;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.core.types.MessageType;
import sdk.chat.licensing.Report;
import sdk.chat.message.sticker.PListLoader;
import sdk.chat.message.sticker.R;
import sdk.chat.message.sticker.StickerPack;
import sdk.chat.message.sticker.integration.BaseStickerMessageHandler;
import sdk.chat.message.sticker.integration.IncomingStickerMessageViewHolder;
import sdk.chat.message.sticker.integration.OutcomingStickerMessageViewHolder;
import sdk.chat.message.sticker.integration.StickerChatOption;
import sdk.chat.message.sticker.integration.StickerMessageHolder;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.custom.CustomMessageHandler;
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

    public List<StickerPack> stickerPacks = new ArrayList<>();



    @Override
    public void activate(Context context) {
        Report.shared().add(getName());

        ChatSDK.a().stickerMessage = new BaseStickerMessageHandler();
        ChatSDK.ui().addChatOption(new StickerChatOption(context.getResources().getString(R.string.sticker_message)));



        ChatSDKUI.shared().getMessageCustomizer().addMessageHandler(new CustomMessageHandler() {

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
                        ChatSDKUI.shared().getMessageCustomizer());
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
                return false;
            }

            @Override
            public boolean onLongClick(Activity activity, View rootView, Message message) {
                return false;
            }
        });

        if (config.loadStickersFromPlist) {
            try {
                stickerPacks = PListLoader.getStickerPacks(context, config.plist);
            } catch (Exception e) {}
        }

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

        public int maxSize = 200;

        public @RawRes int plist = R.raw.default_stickers;

        public boolean loadStickersFromPlist = true;

        public Config(T onBuild) {
            super(onBuild);
        }

        /**
         * Plist to load stickers from
         * @param plist
         * @return
         */
        public Config<T> setPlist(int plist) {
            this.plist = plist;
            return this;
        }

        /**
         * Should we load stickers from a plist file?
         * @param loadStickersFromPlist
         * @return
         */
        public Config<T> setLoadStickersFromPlist(boolean loadStickersFromPlist) {
            this.loadStickersFromPlist = loadStickersFromPlist;
            return this;
        }

        /**
         * Max size of sticker in chat view
         * @param size
         * @return
         */
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

    public List<StickerPack> getStickerPacks() {
        return stickerPacks;
    }

    public void addPack(StickerPack pack) {
        stickerPacks.add(pack);
    }
}
