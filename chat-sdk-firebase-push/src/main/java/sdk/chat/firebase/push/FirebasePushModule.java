package sdk.chat.firebase.push;

import android.content.Context;

import androidx.annotation.NonNull;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.guru.common.BaseConfig;


/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushModule extends AbstractModule {

    public static final FirebasePushModule instance = new FirebasePushModule();

    public static FirebasePushModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<FirebasePushModule> builder() {
        return instance.config;
    }

    public static FirebasePushModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public String firebaseFunctionsRegion;

        public Config(T onBuild) {
            super(onBuild);
        }

        /**
         * Set a custom region
         * @param firebaseFunctionsRegion
         * @return
         */
        public Config<T> setFirebaseFunctionsRegion(String firebaseFunctionsRegion) {
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

    @Override
    public void stop() {
        config = new Config<>(this);
    }

}