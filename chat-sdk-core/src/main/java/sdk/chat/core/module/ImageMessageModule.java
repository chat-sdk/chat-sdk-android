package sdk.chat.core.module;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import sdk.chat.core.base.Base64ImageMessageHandler;
import sdk.chat.core.base.BaseImageMessageHandler;
import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.guru.common.BaseConfig;

public class ImageMessageModule extends AbstractModule {

    public static final ImageMessageModule instance = new ImageMessageModule();

    public static ImageMessageModule shared() {
        return instance;
    }

    public Config<ImageMessageModule> config = new Config<>(this);

    @Override
    public void activate(@NonNull Context context) throws Exception {
        if (config.base64ImagesEnabled) {
            ChatSDK.a().imageMessage = new Base64ImageMessageHandler();
        } else {
            ChatSDK.a().imageMessage = new BaseImageMessageHandler();
        }

    }

    public static Config<ImageMessageModule> builder() {
        return instance.config;
    }

    public static ImageMessageModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public boolean base64ImagesEnabled = false;

        public Config(T onBuild) {
            super(onBuild);
        }

        public boolean getBase64ImagesEnabled() {
            return base64ImagesEnabled;
        }

        public Config<T> setBase64ImagesEnabled(boolean enabled) {
            base64ImagesEnabled = enabled;
            return this;
        }

    }

    @Override
    public MessageHandler getMessageHandler() {
        return ChatSDK.imageMessage();
    }

    @Override
    public List<String> requiredPermissions() {
        return Collections.emptyList();
    }

    @Override
    public boolean isPremium() {
        return false;
    }

    @Override
    public void stop() {

    }
}
