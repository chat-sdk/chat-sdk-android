package sdk.chat.encryption.xmpp;

import android.Manifest;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.encryption.EncryptionHandler;
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
        EncryptionHandler.activate(context);
    }

    @Override
    public void stop() {

    }

    public List<String> requiredPermissions() {
        List<String> permissions= new ArrayList<>();
        permissions.add(Manifest.permission.VIBRATE);
        permissions.add(Manifest.permission.CAMERA);
        return permissions;
    }

    public static Config config() {
        return shared().config;
    }

}
