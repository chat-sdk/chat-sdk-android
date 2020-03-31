package co.chatsdk.firestream;

import android.content.Context;

import androidx.annotation.Nullable;

import org.greenrobot.greendao.annotation.NotNull;

import co.chatsdk.core.base.BaseNetworkAdapter;
import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;
import co.chatsdk.core.session.NetworkAdapterProvider;
import firestream.chat.FirestreamConfig;
import firestream.chat.namespace.Fire;

public class FirestreamModule implements Module, NetworkAdapterProvider {

    public static final FirestreamModule instance = new FirestreamModule();

    public static FirestreamModule shared() {
        return instance;
    }

    public static FirestreamModule shared(@NotNull Configure<FirestreamConfig> configure) {
        configure.with(instance.config);
        return instance;
    }

    protected FirestreamConfig config = new FirestreamConfig();

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
