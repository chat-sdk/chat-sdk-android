package co.chatsdk.firebase.push;

import android.content.Context;

import androidx.annotation.Nullable;

import org.greenrobot.greendao.annotation.NotNull;

import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;
import co.chatsdk.firebase.module.FirebaseConfig;

/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushModule implements Module {

    public static final FirebasePushModule instance = new FirebasePushModule();

    public static FirebasePushModule shared() {
        return instance;
    }

    public static FirebasePushModule shared(@NotNull Configure<Config> configure) {
        configure.with(instance.config);
        return instance;
    }

    public static class Config {

        public String firebaseFunctionsRegion;

        public Config firebaseFunctionsRegion(String firebaseFunctionsRegion) {
            this.firebaseFunctionsRegion = firebaseFunctionsRegion;
            return this;
        }
    }

    protected Config config = new Config();

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