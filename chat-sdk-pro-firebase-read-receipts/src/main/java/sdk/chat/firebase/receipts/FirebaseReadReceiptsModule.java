package sdk.chat.firebase.receipts;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.licensing.Report;
import sdk.guru.common.BaseConfig;


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
    public static Config<FirebaseReadReceiptsModule> builder() {
        return instance.config;
    }

    public static FirebaseReadReceiptsModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public long maxAge = TimeUnit.DAYS.toMillis(7);
        public int maxMessagesPerThread = 20;

        public Config(T onBuild) {
            super(onBuild);
        }

        /**
         * Max age of a message to listen for read receipts
         * @param millis
         * @return
         */
        public Config<T> setMaxAge(long millis) {
            this.maxAge = millis;
            return this;
        }

        /**
         * When we load a thread, we will count back from the last message receive
         * This defines the maximum number of messages to keep the listener active for
         * @param maxMessages
         * @return
         */
        public Config<T> setMaxMessagesPerThread(int maxMessages) {
            this.maxMessagesPerThread = maxMessages;
            return this;
        }
    }

    public Config<FirebaseReadReceiptsModule> config = new Config<>(this);

    @Override
    public void activate(Context context) {
        ChatSDK.a().readReceipts = new FirebaseReadReceiptHandler();
        Report.shared().add(getName());
    }

    public static Config config() {
        return shared().config;
    }

    @Override
    public void stop() {
        config = new Config<>(this);
    }

}
