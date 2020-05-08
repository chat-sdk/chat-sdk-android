package co.chatsdk.firestream;

import android.content.Context;

import androidx.annotation.Nullable;

import sdk.chat.core.base.BaseNetworkAdapter;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.module.Module;
import sdk.chat.core.session.ChatSDK;

import sdk.chat.core.session.Configure;
import sdk.chat.core.session.NetworkAdapterProvider;
import co.chatsdk.firebase.module.FirebaseModule;
import firestream.chat.FirestreamConfig;
import firestream.chat.namespace.Fire;

public class FirestreamModule extends AbstractModule implements NetworkAdapterProvider {

    public static final FirestreamModule instance = new FirestreamModule();

    public static FirestreamModule shared() {
        return instance;
    }

    /**
     * @see FirestreamConfig
     * @return configuration object
     */
    public static FirestreamModule configure(Configure<FirestreamConfig> configure) {
        configure.with(instance.config);
        FirebaseModule.config().setFirebaseRootPath(instance.config.getRoot());
        return instance;
    }

    protected FirestreamConfig config = new FirestreamConfig<>(this);

    @Override
    public void activate(@Nullable Context context) {
        Fire.stream().initialize(ChatSDK.ctx(), config);
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
