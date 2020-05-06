package co.chatsdk.firebase.push;

import android.content.Context;


import androidx.annotation.NonNull;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.module.Module;
import sdk.guru.common.BaseConfig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;


/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushModule extends AbstractModule {

    public static final FirebasePushModule instance = new FirebasePushModule();

    public static FirebasePushModule shared() {
        return instance;
    }

    public static Config<FirebasePushModule> configure() {
        return instance.config;
    }

    public static FirebasePushModule configure(Configure<Config> config) {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public String firebaseFunctionsRegion;

        public Config(T onBuild) {
            super(onBuild);
        }

        public Config<T> firebaseFunctionsRegion(String firebaseFunctionsRegion) {
            this.firebaseFunctionsRegion = firebaseFunctionsRegion;
            return this;
        }
    }

    protected Config<FirebasePushModule> config = new Config<>(this);

    @Override
    public void activate(@NonNull Context context) {
        ChatSDK.a().push = new FirebasePushHandler();
    }

    @Override
    public String getName() {
        return "FirebasePushModule";
    }

    public static Config config() {
        return shared().config;
    }
}