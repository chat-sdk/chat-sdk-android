package sdk.chat.ui.extras;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.module.Module;
import sdk.guru.common.BaseConfig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;


public class ExtrasModule extends AbstractModule {

    public static final ExtrasModule instance = new ExtrasModule();

    public static ExtrasModule shared() {
        return instance;
    }

    public static Config<ExtrasModule> configure() {
        return instance.config;
    }

    public static ExtrasModule configure(Configure<Config> config) {
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

        public Config<T> setDrawerEnabled(boolean value) {
            drawerEnabled = value;
            return this;
        }

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
