package sdk.chat.firebase.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.notifications.NotificationBuilder;
import sdk.chat.core.push.BaseBroadcastHandler;
import sdk.chat.core.push.BroadcastHandler;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.guru.common.BaseConfig;


/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushModule extends AbstractModule {

    public static final FirebasePushModule instance = new FirebasePushModule();

    public static FirebasePushModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<FirebasePushModule> builder() {
        return instance.config;
    }

    public static FirebasePushModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

        public String firebaseFunctionsRegion;
        public BroadcastHandler broadcastHandler;
        public String pushChannelName = "ChatSDK";

        public Config(T onBuild) {
            super(onBuild);
        }

        /**
         * Set a custom region
         * @param firebaseFunctionsRegion
         * @return
         */
        public Config<T> setFirebaseFunctionsRegion(String firebaseFunctionsRegion) {
            this.firebaseFunctionsRegion = firebaseFunctionsRegion;
            return this;
        }

        public Config<T> setBroadcastHandler(BroadcastHandler handler) {
            this.broadcastHandler = handler;
            return this;
        }

        public Config<T> setPushChannelName(String name) {
            this.pushChannelName = name;
            return this;
        }

    }

    protected Config<FirebasePushModule> config = new Config<>(this);

    @Override
    public void activate(@NonNull Context context) {
        ChatSDK.a().push = new FirebasePushHandler();
        ChatSDK.shared().addBroadcastHandler(new BaseBroadcastHandler());



        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library

                NotificationChannel channel = new NotificationChannel(NotificationBuilder.ChatSDKMessageChannel, config().pushChannelName, NotificationManager.IMPORTANCE_HIGH);
                channel.enableVibration(true);

//                if (channelDescription != null) {
//                    channel.setDescription(channelDescription);
//                }
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }

    }

    public static Config config() {
        return shared().config;
    }

    @Override
    public void stop() {
        config = new Config<>(this);
    }

    public boolean isPremium() {
        return false;
    }

}