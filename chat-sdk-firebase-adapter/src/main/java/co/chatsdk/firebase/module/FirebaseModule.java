package co.chatsdk.firebase.module;

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

    public static FirebaseModule shared(@NotNull Configure<FirebaseConfig> configure) {
        configure.with(instance.config);
        return instance;
    }

    public FirebaseConfig config = new FirebaseConfig();

    @Override
    public void activate() {

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
