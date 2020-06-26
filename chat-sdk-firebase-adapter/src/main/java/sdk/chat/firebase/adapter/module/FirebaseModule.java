package sdk.chat.firebase.adapter.module;

import android.content.Context;

import androidx.annotation.NonNull;

import sdk.chat.core.base.BaseNetworkAdapter;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.Configure;
import sdk.chat.core.session.NetworkAdapterProvider;

public class FirebaseModule extends AbstractModule implements NetworkAdapterProvider {

    public static final FirebaseModule instance = new FirebaseModule();

    public static FirebaseModule shared() {
        return instance;
    }

    /**
     * @see FirebaseConfig
     * @return configuration object
     */
    public static FirebaseConfig<FirebaseModule> builder() {
        return instance.config;
    }

    public static FirebaseModule builder(Configure<FirebaseConfig> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public FirebaseConfig<FirebaseModule> config = new FirebaseConfig<>(this);

    @Override
    public void activate(@NonNull Context context) {

    }

    @Override
    public String getName() {
        return "FirebaseModule";
    }

    @Override
    public void stop() {
        config = new FirebaseConfig<>(this);
    }

    @Override
    public Class<? extends BaseNetworkAdapter> getNetworkAdapter() {
        return config.networkAdapter;
    }

    public static FirebaseConfig config() {
        return shared().config;
    }


}
