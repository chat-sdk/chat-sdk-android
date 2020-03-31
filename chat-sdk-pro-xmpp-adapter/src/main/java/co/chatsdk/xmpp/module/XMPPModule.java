package co.chatsdk.xmpp.module;

import android.content.Context;

import androidx.annotation.Nullable;

import co.chatsdk.core.base.BaseNetworkAdapter;
import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.interfaces.InterfaceAdapter;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;
import co.chatsdk.core.session.InterfaceAdapterProvider;
import co.chatsdk.core.session.NetworkAdapterProvider;
import co.chatsdk.ui.module.DefaultUIModule;
import co.chatsdk.ui.module.UIConfig;
import co.chatsdk.xmpp.handlers.XMPPNetworkAdapter;
import co.chatsdk.xmpp.ui.XMPPInterfaceAdapter;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class XMPPModule implements Module, NetworkAdapterProvider, InterfaceAdapterProvider {

    protected static final XMPPModule instance = new XMPPModule();

    public static XMPPModule shared() {
        return instance;
    }

    public static XMPPModule shared(Configure<XMPPConfig> configure, @Nullable Configure<UIConfig> configureUI) {
        configure.with(instance.config);
        if (configureUI != null) {
            configureUI.with(DefaultUIModule.config());
        }
        return instance;
    }

    public XMPPConfig config = new XMPPConfig();

    @Override
    public void activate(Context context) {

    }

    @Override
    public String getName() {
        return "XMPPModule";
    }

    @Override
    public Class<? extends InterfaceAdapter> getInterfaceAdapter() {
        return XMPPInterfaceAdapter.class;
    }

    @Override
    public Class<? extends BaseNetworkAdapter> getNetworkAdapter() {
        return XMPPNetworkAdapter.class;
    }

    public static XMPPConfig config() {
        return shared().config;
    }
}
