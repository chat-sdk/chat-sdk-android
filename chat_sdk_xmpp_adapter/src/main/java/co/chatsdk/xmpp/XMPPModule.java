package co.chatsdk.xmpp;

import co.chatsdk.core.InterfaceManager;
import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.handlers.Module;
import co.chatsdk.xmpp.handlers.XMPPNetworkAdapter;
import co.chatsdk.xmpp.ui.XMPPInterfaceAdapter;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class XMPPModule implements Module {
    @Override
    public void activate() {
        NetworkManager.shared().a = new XMPPNetworkAdapter();
        InterfaceManager.shared().a = new XMPPInterfaceAdapter();
    }
}
