package co.chatsdk.firestream;

import android.content.Context;

import androidx.annotation.Nullable;

import co.chatsdk.firebase.module.FirebaseModule;
import firestream.chat.FirestreamConfig;
import firestream.chat.namespace.Fire;
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
     * @see FirestreamConfig
     * @return configuration object
     */
    public static FireStreamModule builder(Configure<FirestreamConfig> configure) {
        configure.with(instance.config);
        return instance;
    }

    public static FirestreamConfig<FireStreamModule> builder() {
        return instance.config;
    }

    protected FirestreamConfig config = new FirestreamConfig<>(this);

    @Override
    public void activate(@Nullable Context context) {
        Fire.stream().initialize(ChatSDK.ctx(), config);
        FirebaseModule.config().setFirebaseRootPath(config.getRoot());
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
