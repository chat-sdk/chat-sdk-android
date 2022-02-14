package sdk.chat.sinch;

import android.Manifest;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.RemoteMessage;
import com.sinch.android.rtc.SinchHelpers;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.module.Module;
import sdk.chat.core.push.PushMessage;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.firebase.push.FirebaseBroadcastHandler;
import sdk.guru.common.BaseConfig;
import sdk.guru.common.RX;

public class SinchModule implements Module {

    protected static final SinchModule instance = new SinchModule();
    public static SinchModule shared() {
        return instance;
    }

    public static Config<SinchModule> config() {
        return shared().config;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<SinchModule> builder() {
        return instance.config;
    }

    public static SinchModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public Config<SinchModule> config = new Config<>(this);

    public SinchService sinchService = new SinchService();

    public static class Config<T> extends BaseConfig<T> {

        /**
         * Maximum distance to pick up nearby users
         */
        public String applicationKey;
        public String secret;
        public String environmentHost = "ocra.api.sinch.com";
        public JWTProvider jwtProvider = new JWTProvider() {
            @Override
            public Single<String> getJWT(String userId) {
                return Single.create((SingleOnSubscribe<String>) emitter -> {
                    emitter.onSuccess(JWT.create(applicationKey, secret, userId));
                });
            }
        };

        public Config(T onBuild) {
            super(onBuild);
        }

        public Config<T> setApplicationKey(String value) {
            this.applicationKey = value;
            return this;
        }

        public Config<T> setSecret(String value) {
            this.secret = value;
            return this;
        }

        public Config<T> setJWTProvider(JWTProvider value) {
            this.jwtProvider = value;
            return this;
        }

        public Config<T> setEnvironmentHost(String value) {
            this.environmentHost = value;
            return this;
        }

    }

    @Override
    public void activate(@NonNull Context context) throws Exception {

        ChatSDK.hook().addHook(Hook.sync(data -> {
            RX.main().scheduleDirect(() -> {
                sinchService.start(context, ChatSDK.currentUserID());
            });
        }), HookEvent.DidAuthenticate);

        ChatSDK.hook().addHook(Hook.sync(data -> {
            RX.main().scheduleDirect(() -> {
                sinchService.stop();
            });
        }), HookEvent.DidLogout);

//        ChatSDKUI.setMainActivity(SinchMainAppBarActivity.class);
        ChatSDK.a().call = new SinchCallHandler();

        // Add a broadcast handler for sinch messages
        ChatSDK.shared().addBroadcastHandler(new FirebaseBroadcastHandler() {
            @Override
            public boolean onReceive(RemoteMessage message) {
                if (SinchHelpers.isSinchPushPayload(message.getData())) {
                    sinchService.client().relayRemotePushNotificationPayload(message.getData());
                    return true;
                }
                return false;
            }

            @Override public boolean onReceive(PushMessage message) { return false; }
        }, 0);

    }

    @Override
    public String getName() {
        return "SinchCalling";
    }

    @Override
    public MessageHandler getMessageHandler() {
        return null;
    }

    @Override
    public List<String> requiredPermissions() {
        return new ArrayList<String>() {{
            add(Manifest.permission.INTERNET);
            add(Manifest.permission.ACCESS_NETWORK_STATE);
            add(Manifest.permission.RECORD_AUDIO);
            add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
            add(Manifest.permission.READ_PHONE_STATE);
        }};
    }

    @Override
    public boolean isPremium() {
        return true;
    }

    @Override
    public void stop() {

    }
}
