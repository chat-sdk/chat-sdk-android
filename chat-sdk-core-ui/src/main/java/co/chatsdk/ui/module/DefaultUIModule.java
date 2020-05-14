package co.chatsdk.ui.module;

import android.content.Context;

import androidx.annotation.Nullable;

import co.chatsdk.ui.BaseInterfaceAdapter;
import sdk.chat.core.interfaces.InterfaceAdapter;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.core.session.InterfaceAdapterProvider;
import sdk.chat.core.utils.StringChecker;

public class DefaultUIModule extends AbstractModule implements InterfaceAdapterProvider {

    public static final DefaultUIModule instance = new DefaultUIModule();

    public static DefaultUIModule shared() {
        return instance;
    }

    /**
     * @see UIConfig
     * @return configuration object
     */
    public static UIConfig<DefaultUIModule> builder() {
        return instance.config;
    }

    public static DefaultUIModule builder(Configure<UIConfig> config) {
        config.with(instance.config);
        return instance;
    }

    public UIConfig<DefaultUIModule> config = new UIConfig<>(this);

    @Override
    public void activate(@Nullable Context context) {
        if (StringChecker.isNullOrEmpty(ChatSDK.config().googleMapsApiKey)) {
            config.locationMessagesEnabled = false;
        }
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
