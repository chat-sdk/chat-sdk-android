package sdk.chat.sinch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.SinchHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.module.Module;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.chat.sinch.calling.IncomingCallActivity;
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
        ChatSDK.shared().addBroadcastHandler((ctx, intent) -> {
            Map<String, String> data = new HashMap<>();
            for (String key: intent.getExtras().keySet()) {
                String value = intent.getExtras().getString(key);
                if (value != null) {
                    data.put(key, value);
                }
            }

            if (SinchHelpers.isSinchPushPayload(data)) {
                NotificationResult result = sinchService.client().relayRemotePushNotificationPayload(data);
                if (result.isValid() && ChatSDK.appBackgroundMonitor().inBackground()) {
                    Intent appIntent = new Intent(context, IncomingCallActivity.class);
                    appIntent.putExtra(SinchService.CALL_ID, result.getCallResult().getCallId());
                    appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ChatSDK.ui().notificationDisplayHandler().createCallNotification(context, appIntent, result.getCallResult().getRemoteUserId(), result.getDisplayName());
                    return true;
                }
            }
            return false;
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
