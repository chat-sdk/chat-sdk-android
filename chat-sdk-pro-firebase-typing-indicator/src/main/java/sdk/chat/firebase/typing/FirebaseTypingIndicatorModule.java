package sdk.chat.firebase.typing;


import android.content.Context;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.licensing.Report;
import sdk.guru.common.BaseConfig;

/**
 * Created by ben on 10/5/17.
 */

public class FirebaseTypingIndicatorModule extends AbstractModule {

    public static final FirebaseTypingIndicatorModule instance = new FirebaseTypingIndicatorModule();

    public static FirebaseTypingIndicatorModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<FirebaseTypingIndicatorModule> builder() {
        return instance.config;
    }

    public static FirebaseTypingIndicatorModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public long typingTimeout  = 3000;

        public Config(T onBuild) {
            super(onBuild);
        }

        /**
         * Typing indicator timeout in millis
         * @param typingTimeout
         * @return
         */
        public Config<T> setTypingTimeout(long typingTimeout) {
            this.typingTimeout = typingTimeout;
            return this;
        }

    }

    protected Config<FirebaseTypingIndicatorModule> config = new Config<>(this);

    @Override
    public void activate(Context context) {
        ChatSDK.a().typingIndicator = new FirebaseTypingIndicatorHandler();
        Report.shared().add(getName());
    }

    public static Config config() {
        return instance.config;
    }

    @Override
    public void stop() {
        config = new Config<>(this);
    }

}
