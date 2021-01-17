package sdk.chat.encryption;

import android.content.Context;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.licensing.Report;
import sdk.guru.common.BaseConfig;


/*
Created by Conrad on 15.11.2018
 */

public class EncryptionModule extends AbstractModule {

    public static final EncryptionModule instance = new EncryptionModule();

    public static EncryptionModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<EncryptionModule> builder() {
        return instance.config;
    }

    public static EncryptionModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public Config(T onBuild) {
            super(onBuild);
        }
    }

    public Config<EncryptionModule> config = new Config<>(this);

    @Override
    public void activate(Context context) {
        ChatSDK.a().encryption = new EncryptionHandler();
        Report.shared().add(getName());
    }

    @Override
    public String getName() {
        return "EncryptionModule";
    }

    @Override
    public void stop() {

    }

    public static Config config() {
        return shared().config;
    }

}


