package co.chatsdk.encryption;

import android.content.Context;

import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;

/*
Created by Conrad on 15.11.2018
 */

public class EncryptionModule implements Module {

    public static final EncryptionModule instance = new EncryptionModule();

    public static EncryptionModule shared() {
        return instance;
    }

    public static EncryptionModule shared(Configure<Config> configure) {
        configure.with(instance.config);
        return instance;
    }

    public static class Config {

    }

    public Config config = new Config();

    @Override
    public void activate(Context context) {
        ChatSDK.a().encryption = new BaseEncryptionHandler();
    }

    @Override
    public String getName() {
        return "EncryptionModule";
    }

    public Config config() {
        return config;
    }
}


