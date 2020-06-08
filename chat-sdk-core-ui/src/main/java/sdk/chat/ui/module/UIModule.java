package sdk.chat.ui.module;

import android.Manifest;
import android.content.Context;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

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
        return config.interfaceAdapter;
    }

    public static UIConfig config() {
        return shared().config;
    }

    public List<String> requiredPermissions() {
        List<String> permissions = new ArrayList<>();

        if (config.locationMessagesEnabled) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.INTERNET);

        permissions.add(Manifest.permission.CAMERA);

        if (ChatSDK.audioMessage() != null) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }

        return permissions;
    }


}
