package co.chatsdk.firebase.file_storage;

import org.greenrobot.greendao.annotation.NotNull;

import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;
import co.chatsdk.firebase.module.FirebaseConfig;

/**
 * Created by ben on 9/1/17.
 */

public class FirebaseFileStorageModule implements Module {

    public static final FirebaseFileStorageModule instance = new FirebaseFileStorageModule();

    public static FirebaseFileStorageModule shared() {
        return instance;
    }

    public static FirebaseFileStorageModule shared(@NotNull Configure<Config> configure) {
        configure.with(instance.config);
        return instance;
    }

    public static class Config {
        public String firebaseStorageUrl;

        public Config firebaseStorageURL(String firebaseStorage) {
            this.firebaseStorageUrl = firebaseStorage;
            return this;
        }
    }

    protected Config config = new Config();

    @Override
    public void activate() {
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
