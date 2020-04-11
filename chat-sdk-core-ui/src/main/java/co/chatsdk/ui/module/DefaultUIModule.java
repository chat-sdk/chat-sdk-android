package co.chatsdk.ui.module;

import android.content.Context;

import androidx.annotation.Nullable;

import sdk.chat.core.handlers.Module;
import sdk.chat.core.interfaces.InterfaceAdapter;

import sdk.chat.core.session.Configure;
import sdk.chat.core.session.InterfaceAdapterProvider;
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
