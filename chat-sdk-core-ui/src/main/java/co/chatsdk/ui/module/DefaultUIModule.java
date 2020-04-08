package co.chatsdk.ui.module;

import android.content.Context;

import androidx.annotation.Nullable;

import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.interfaces.InterfaceAdapter;

import co.chatsdk.core.session.Configure;
import co.chatsdk.core.session.InterfaceAdapterProvider;
import co.chatsdk.ui.BaseInterfaceAdapter;

public class DefaultUIModule implements Module, InterfaceAdapterProvider {

    public static final DefaultUIModule instance = new DefaultUIModule();

    public static DefaultUIModule shared() {
        return instance;
    }

    public static UIConfig<DefaultUIModule> configure() {
        return instance.config;
    }

    public static DefaultUIModule configure(Configure<UIConfig> config) {
        config.with(instance.config);
        return instance;
    }

    public UIConfig<DefaultUIModule> config = new UIConfig<>(this);

    @Override
    public void activate(@Nullable Context context) {

    }

    @Override
    public String getName() {
        return "ChatSDKUIModule";
    }

    @Override
    public Class<? extends InterfaceAdapter> getInterfaceAdapter() {
        return BaseInterfaceAdapter.class;
    }

    public static UIConfig config() {
        return shared().config;
    }

}
