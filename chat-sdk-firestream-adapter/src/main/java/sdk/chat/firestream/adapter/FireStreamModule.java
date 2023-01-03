package sdk.chat.firestream.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

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
import sdk.chat.firebase.adapter.module.FirebaseModule;

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
    public static FireStreamModule builder(FirebaseServiceType serviceType, Configure<Config> configure) throws Exception {
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
    public void activate(@Nullable Context context) throws Exception {
        FirebaseService service = config.serviceType == FirebaseServiceType.Firestore ? new FirestoreService() : new RealtimeService();
        config.emitEventForLastMessage = true;
        Fire.stream().initialize(ChatSDK.ctx(), config, service);
        FirebaseModule.config().setFirebaseRootPath(config.getRoot());
    }

    public static class Config<T> extends FirestreamConfig<T> {

        public Config(T onBuild) {
            super(onBuild);
        }

        public FirebaseServiceType serviceType = FirebaseServiceType.Firestore;

        public Class<? extends BaseNetworkAdapter> networkAdapter = FireStreamNetworkAdapter.class;

        /**
         * Firebase service type, Firestore or Realtime database
         * @param type
         * @return
         */
        public Config<T> setFirebaseService(FirebaseServiceType type) {
            this.serviceType = type;
            return this;
        }

        /**
         * Override the Firestream network adapter class
         * @param networkAdapter
         * @return
         */
        public Config<T> setNetworkAdapter(Class<? extends BaseNetworkAdapter> networkAdapter) {
            this.networkAdapter = networkAdapter;
            return this;
        }

    }

    @Override
    public Class<? extends BaseNetworkAdapter> getNetworkAdapter() {
        return config.networkAdapter;
    }

    @Override
    public void stop() {
        config = new Config<>(this);
    }

    public boolean isPremium() {
        return false;
    }

}
