package app.xmpp.adapter.module;

import android.content.Context;

import app.xmpp.adapter.R;
import app.xmpp.adapter.ui.XMPPInterfaceAdapter;
import sdk.chat.core.base.BaseNetworkAdapter;
import sdk.chat.core.interfaces.InterfaceAdapter;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.Configure;
import sdk.chat.core.session.InterfaceAdapterProvider;
import sdk.chat.core.session.NetworkAdapterProvider;
import sdk.chat.licensing.Report;
import sdk.chat.ui.module.UIConfig;
import sdk.chat.ui.module.UIModule;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class XMPPModule extends AbstractModule implements NetworkAdapterProvider, InterfaceAdapterProvider {

    protected static final XMPPModule instance = new XMPPModule();

    public static XMPPModule shared() {
        return instance;
    }

    /**
     * @see XMPPConfig
     * @return configuration object
     */
    public static XMPPConfig<XMPPModule> builder() {
        return instance.config;
    }

    public XMPPModule configureUI(Configure<UIConfig> configure) throws Exception {
        configure.with(UIModule.config());
        return instance;
    }

    public XMPPConfig<XMPPModule> config = new XMPPConfig<>(this);

    @Override
    public void activate(Context context) {
        Report.shared().add(getName());
        if (UIModule.config().usernameHint == null) {
            UIModule.config().usernameHint = context.getString(R.string.user_jid);
        }
        UIModule.config().customizeGroupImageEnabled = false;
//        UIModule.config().messageReplyEnabled = false;
//        UIModule.config().messageForwardingEnabled = false;

//        ChatSDK.hook().addHook(Hook.sync(data -> {
//            OMEMOModule.shared().start();
//        }), HookEvent.DidAuthenticate);

    }

    public void setupOmemo() {


    }

    @Override
    public Class<? extends InterfaceAdapter> getInterfaceAdapter() {
        return XMPPInterfaceAdapter.class;
    }

    @Override
    public Class<? extends BaseNetworkAdapter> getNetworkAdapter() {
        return config.networkAdapter;
    }

    public static XMPPConfig config() {
        return shared().config;
    }

    @Override
    public void stop() {
        config = new XMPPConfig<>(this);
    }

}
