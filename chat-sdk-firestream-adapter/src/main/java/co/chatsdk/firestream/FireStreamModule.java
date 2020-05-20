package co.chatsdk.firestream;

import android.content.Context;

import androidx.annotation.Nullable;

import co.chatsdk.firebase.module.FirebaseModule;
import firestream.chat.FirestreamConfig;
import firestream.chat.firebase.service.FirebaseService;
import firestream.chat.firestore.FirestoreService;
import firestream.chat.namespace.Fire;
import firestream.chat.realtime.RealtimeService;
import sdk.chat.core.base.BaseNetworkAdapter;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.core.session.NetworkAdapterProvider;

public class FireStreamModule extends AbstractModule implements NetworkAdapterProvider {

    public static final FireStreamModule instance = new FireStreamModule();

    public static FireStreamModule shared() {
        return instance;
    }

    /**
     * @param serviceType - Firestore or Realtime
     * @param configure
     * @return
     */
    public static FireStreamModule builder(FirebaseServiceType serviceType, Configure<Config> configure) {
        instance.config.serviceType = serviceType;
        configure.with(instance.config);
        return instance;
    }

    /**
     * @param serviceType - Firestore or Realtime
     * @return
     */
    public static Config<FireStreamModule> builder(FirebaseServiceType serviceType) {
        instance.config.serviceType = serviceType;
        return instance.config;
    }

    public Config<FireStreamModule> config = new Config<>(this);

    @Override
    public void activate(@Nullable Context context) {
        FirebaseService service = config.serviceType == FirebaseServiceType.Firestore ? new FirestoreService() : new RealtimeService();
        Fire.stream().initialize(ChatSDK.ctx(), config, service);
        FirebaseModule.config().setFirebaseRootPath(config.getRoot());
    }

    public static class Config<T> extends FirestreamConfig<T> {

        public Config(T onBuild) {
            super(onBuild);
        }

        public FirebaseServiceType serviceType = FirebaseServiceType.Firestore;

        /**
         * Firebase service type, Firestore or Realtime database
         * @param type
         * @return
         */
        public Config<T> setFirebaseService(FirebaseServiceType type) {
            this.serviceType = type;
            return this;
        }
    }

    @Override
    public String getName() {
        return "FirestreamModule";
    }

    @Override
    public Class<? extends BaseNetworkAdapter> getNetworkAdapter() {
        return FireStreamNetworkAdapter.class;
    }

}
