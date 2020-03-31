package co.chatsdk.read_receipts;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;

/**
 * Created by ben on 10/5/17.
 */

public class FirebaseReadReceiptsModule implements Module {

    public static final FirebaseReadReceiptsModule instance = new FirebaseReadReceiptsModule();

    public static FirebaseReadReceiptsModule shared() {
        return instance;
    }

    public static FirebaseReadReceiptsModule shared(Configure<Config> configure) {
        configure.with(instance.config);
        return instance;
    }

    public static class Config {

        public long maxAge = TimeUnit.DAYS.toMillis(7);

        public Config readReceiptMaxAge(long millis) {
            this.maxAge = millis;
            return this;
        }
    }

    public Config config = new Config();

    @Override
    public void activate(Context context) {
        ChatSDK.a().readReceipts = new FirebaseReadReceiptHandler();
    }

    @Override
    public String getName() {
        return "FirebaseReadReceiptsModule";
    }

    public static Config config() {
        return shared().config;
    }

}
