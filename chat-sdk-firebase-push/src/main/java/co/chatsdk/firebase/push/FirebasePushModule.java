package co.chatsdk.firebase.push;

import android.content.Context;

import org.greenrobot.greendao.annotation.NotNull;

import co.chatsdk.core.handlers.Module;
import sdk.guru.common.BaseConfig;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;


/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushModule implements Module {

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
    public void activate(@NotNull Context context) {
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