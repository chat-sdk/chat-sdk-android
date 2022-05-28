package sdk.chat.message.sticker.module;

import android.content.Context;

import androidx.annotation.RawRes;

import feather.Provides;
import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.licensing.Report;
import sdk.chat.message.sticker.R;
import sdk.chat.message.sticker.StickerMessageRegistration;
import sdk.chat.message.sticker.integration.BaseStickerMessageHandler;
import sdk.chat.message.sticker.integration.StickerChatOption;
import sdk.chat.message.sticker.provider.PListStickerPackProvider;
import sdk.chat.message.sticker.provider.StickerPackProvider;
import sdk.chat.ui.ChatSDKUI;
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
        Report.shared().add(getName());

        ChatSDK.a().stickerMessage = new BaseStickerMessageHandler();
        ChatSDK.ui().addChatOption(new StickerChatOption(R.string.sticker, R.drawable.icn_100_sticker));

        ChatSDKUI.shared().getMessageRegistrationManager().addMessageRegistration(new StickerMessageRegistration());

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

        public int maxSize = 170;

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


    @Provides
    public StickerPackProvider getStickerPack() {
        return new PListStickerPackProvider();
    }

}
