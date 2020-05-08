package co.chatsdk.read_receipts;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.module.Module;
import sdk.guru.common.BaseConfig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;


/**
 * Created by ben on 10/5/17.
 */

public class FirebaseReadReceiptsModule extends AbstractModule {

    public static final FirebaseReadReceiptsModule instance = new FirebaseReadReceiptsModule();

    public static FirebaseReadReceiptsModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<FirebaseReadReceiptsModule> configure() {
        return instance.config;
    }

    public static FirebaseReadReceiptsModule configure(Configure<Config> config) {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public long maxAge = TimeUnit.DAYS.toMillis(7);
        public int maxMessagesPerThread = 20;

        public Config(T onBuild) {
            super(onBuild);
        }

        public Config<T> setMaxAge(long millis) {
            this.maxAge = millis;
            return this;
        }

        public Config<T> setMaxMessagesPerThread(int maxMessages) {
            this.maxMessagesPerThread = maxMessages;
            return this;
        }
    }

    public Config<FirebaseReadReceiptsModule> config = new Config<>(this);

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
