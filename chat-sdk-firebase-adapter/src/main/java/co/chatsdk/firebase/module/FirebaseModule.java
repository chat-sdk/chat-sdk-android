package co.chatsdk.firebase.module;

import android.content.Context;

import org.greenrobot.greendao.annotation.NotNull;

import co.chatsdk.core.base.BaseNetworkAdapter;
import co.chatsdk.core.handlers.Module;

import co.chatsdk.core.session.Configure;
import co.chatsdk.core.session.NetworkAdapterProvider;
import co.chatsdk.firebase.FirebaseNetworkAdapter;

public class FirebaseModule implements Module, NetworkAdapterProvider {

    public static final FirebaseModule instance = new FirebaseModule();

    public static FirebaseModule shared() {
        return instance;
    }

    public static FirebaseConfig<FirebaseModule> configure() {
        return instance.config;
    }

    public static FirebaseModule configure(Configure<FirebaseConfig> config) {
        config.with(instance.config);
        return instance;
    }

    public FirebaseConfig<FirebaseModule> config = new FirebaseConfig<>(this);

    @Override
    public void activate(@NotNull Context context) {

    }

    @Override
    public String getName() {
        return "FirebaseLegacyNetworkAdapterModule";
    }

    @Override
    public Class<? extends BaseNetworkAdapter> getNetworkAdapter() {
        return FirebaseNetworkAdapter.class;
    }

    public static FirebaseConfig config() {
        return shared().config;
    }

}
