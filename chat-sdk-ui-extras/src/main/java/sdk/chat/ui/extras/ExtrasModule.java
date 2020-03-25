package sdk.chat.ui.extras;

import androidx.annotation.DrawableRes;

import org.greenrobot.greendao.annotation.NotNull;

import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;
import co.chatsdk.ui.module.UIConfig;

public class ExtrasModule implements Module {

    public static final ExtrasModule instance = new ExtrasModule();

    public static ExtrasModule shared() {
        return instance;
    }

    public static ExtrasModule shared(@NotNull Configure<Config> configure) {
        configure.with(instance.config);
        return instance;
    }

    public static class Config {

        public boolean drawerEnabled = true;

        /**
         * Default image drawer header area
         */
        @DrawableRes
        public int drawerHeaderImage = R.drawable.header2;

        public Config setDrawerEnabled(boolean value) {
            drawerEnabled = value;
            return this;
        }

        public Config setDrawerHeaderImage(@DrawableRes int res) {
            drawerHeaderImage = res;
            return this;
        }

    }

    protected Config config = new Config();

    @Override
    public void activate() {
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
