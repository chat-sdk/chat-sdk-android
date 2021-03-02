package sdk.chat.encryption.xmpp;

import android.content.Context;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.licensing.Report;
import sdk.guru.common.BaseConfig;

public class XMPPEncryptionModule extends AbstractModule {

    public static final XMPPEncryptionModule instance = new XMPPEncryptionModule();

    public static XMPPEncryptionModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<XMPPEncryptionModule> builder() {
        return instance.config;
    }

    public static XMPPEncryptionModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public Config(T onBuild) {
            super(onBuild);
        }
    }

    public Config<XMPPEncryptionModule> config = new Config<>(this);

    @Override
    public void activate(Context context) {
        ChatSDK.a().encryption = new XMPPEncryptionHandler();
        Report.shared().add(getName());
    }

    @Override
    public void stop() {

    }

    public static Config config() {
        return shared().config;
    }


}
