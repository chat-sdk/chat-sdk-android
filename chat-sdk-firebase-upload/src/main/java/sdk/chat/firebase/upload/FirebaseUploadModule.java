package sdk.chat.firebase.upload;

import android.content.Context;

import androidx.annotation.NonNull;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.guru.common.BaseConfig;


/**
 * Created by ben on 9/1/17.
 */

public class FirebaseUploadModule extends AbstractModule {

    public static final FirebaseUploadModule instance = new FirebaseUploadModule();

    public static FirebaseUploadModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<FirebaseUploadModule> builder() {
        return instance.config;
    }

    public static FirebaseUploadModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {
        public String firebaseStorageUrl;

        public Config(T onBuild) {
            super(onBuild);
        }

        /**
         * Use a custom URL for Firebase storage
         * @param firebaseStorage
         * @return
         */
        public Config<T> setFirebaseStorageURL(String firebaseStorage) {
            this.firebaseStorageUrl = firebaseStorage;
            return this;
        }
    }

    protected Config<FirebaseUploadModule> config = new Config<>(this);

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

    @Override
    public void stop() {
        config = new Config<>(this);
    }

}
