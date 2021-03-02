package sdk.chat.encryption.firebase;

import android.content.Context;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.encryption.xmpp.XMPPEncryptionHandler;
import sdk.chat.licensing.Report;
import sdk.guru.common.BaseConfig;

public class FirebaseEncryptionModule extends AbstractModule {

    public static final FirebaseEncryptionModule instance = new FirebaseEncryptionModule();

    public static FirebaseEncryptionModule shared() {
        return instance;
    }

    /**
     * @see FirebaseEncryptionModule.Config
     * @return configuration object
     */
    public static Config<FirebaseEncryptionModule> builder() {
        return instance.config;
    }

    public static FirebaseEncryptionModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public Config(T onBuild) {
            super(onBuild);
        }
    }

    public FirebaseEncryptionModule.Config<FirebaseEncryptionModule> config = new Config<>(this);

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
