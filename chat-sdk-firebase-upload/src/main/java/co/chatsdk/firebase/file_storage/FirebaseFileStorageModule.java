package co.chatsdk.firebase.file_storage;

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

public class FirebaseFileStorageModule extends AbstractModule {

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
    public void activate(@NonNull Context context) {
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
