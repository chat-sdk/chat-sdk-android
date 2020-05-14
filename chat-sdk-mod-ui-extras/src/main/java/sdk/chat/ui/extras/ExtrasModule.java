package sdk.chat.ui.extras;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.guru.common.BaseConfig;


public class ExtrasModule extends AbstractModule {

    public static final ExtrasModule instance = new ExtrasModule();

    public static ExtrasModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<ExtrasModule> builder() {
        return instance.config;
    }

    public static ExtrasModule builder(Configure<Config> config) {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public boolean drawerEnabled = true;

        /**
         * Default image drawer header area
         */
        @DrawableRes
        public int drawerHeaderImage = R.drawable.header2;

        public Config(T onBuild) {
            super(onBuild);
        }

        /**
         * Enable the navigation drawer
         * @param value
         * @return
         */
        public Config<T> setDrawerEnabled(boolean value) {
            drawerEnabled = value;
            return this;
        }

        /**
         * Set the default drawer header image
         * @param res
         * @return
         */
        public Config<T> setDrawerHeaderImage(@DrawableRes int res) {
            drawerHeaderImage = res;
            return this;
        }

    }

    protected Config<ExtrasModule> config = new Config<>(this);

    @Override
    public void activate(@Nullable Context context) {
        if (config.drawerEnabled) {
            ChatSDK.ui().setMainActivity(MainDrawActivity.class);
        }
    }

    @Override
    public String getName() {
        return "ExtrasModule";
    }

    public static Config config() {
        return shared().config;
    }
}
