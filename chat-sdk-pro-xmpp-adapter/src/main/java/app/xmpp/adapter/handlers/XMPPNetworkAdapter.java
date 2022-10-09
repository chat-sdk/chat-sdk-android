package app.xmpp.adapter.handlers;

import sdk.chat.core.base.BaseNetworkAdapter;

/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class XMPPNetworkAdapter extends BaseNetworkAdapter {

    public XMPPNetworkAdapter () {

        this.core = new XMPPCoreHandler();
        this.auth = new XMPPAuthenticationHandler();
        this.events = new XMPPEventHandler();
        this.thread = new XMPPThreadHandler();
        this.search = new XMPPSearchHandler();
        this.contact = new XMPPContactHandler();
        this.blocking = new XMPPBlockingHandler();
        this.typingIndicator = new XMPPTypingIndicatorHandler();
        this.lastOnline = new XMPPLastOnlineHandler();
        this.publicThread = new XMPPPublicThreadHandler();
    }

}
