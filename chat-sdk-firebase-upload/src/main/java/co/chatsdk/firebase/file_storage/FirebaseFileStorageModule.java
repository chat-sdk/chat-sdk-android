package co.chatsdk.firebase.file_storage;

import android.content.Context;

import org.greenrobot.greendao.annotation.NotNull;

import co.chatsdk.core.handlers.Module;
import sdk.guru.common.BaseConfig;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;


/**
 * Created by ben on 9/1/17.
 */

public class FirebaseFileStorageModule implements Module {

    public static final FirebaseFileStorageModule instance = new FirebaseFileStorageModule();

    public static FirebaseFileStorageModule shared() {
        return instance;
    }

    public static Config<FirebaseFileStorageModule> configure() {
        return instance.config;
    }

    public static FirebaseFileStorageModule configure(Configure<Config> config) {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {
        public String firebaseStorageUrl;

        public Config(T onBuild) {
            super(onBuild);
        }

        public Config<T> firebaseStorageURL(String firebaseStorage) {
            this.firebaseStorageUrl = firebaseStorage;
            return this;
        }
    }

    protected Config<FirebaseFileStorageModule> config = new Config<>(this);

    @Override
    public void activate(@NotNull Context context) {
        ChatSDK.a().upload = new FirebaseUploadHandler();
    }

    @Override
    public String getName() {
        return "FirebaseFileStorageModule";
    }

    public static Config config() {
        return shared().config;
    }

}
