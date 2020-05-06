package co.chatsdk.xmpp.module;

import android.content.Context;

import sdk.chat.core.base.BaseNetworkAdapter;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.module.Module;
import sdk.chat.core.interfaces.InterfaceAdapter;

import sdk.chat.core.session.Configure;
import sdk.chat.core.session.InterfaceAdapterProvider;
import sdk.chat.core.session.NetworkAdapterProvider;
import co.chatsdk.ui.module.DefaultUIModule;
import co.chatsdk.ui.module.UIConfig;
import co.chatsdk.xmpp.R;
import co.chatsdk.xmpp.handlers.XMPPNetworkAdapter;
import co.chatsdk.xmpp.ui.XMPPInterfaceAdapter;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class XMPPModule extends AbstractModule implements NetworkAdapterProvider, InterfaceAdapterProvider {

    protected static final XMPPModule instance = new XMPPModule();

    public static XMPPModule shared() {
        return instance;
    }

    public static XMPPConfig<XMPPModule> configure() {
        return instance.config;
    }

    public XMPPModule configureUI(Configure<UIConfig> configure) {
        configure.with(DefaultUIModule.config());
        return instance;
    }

    public XMPPConfig<XMPPModule> config = new XMPPConfig<>(this);

    @Override
    public void activate(Context context) {
        if ( DefaultUIModule.config().usernameHint == null) {
            DefaultUIModule.config().usernameHint = context.getString(R.string.user_jid);
        }
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
