package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.ReconnectionListener;
import org.pmw.tinylog.Logger;


public class XMPPReconnectionListener implements ReconnectionListener {
    @Override
    public void reconnectingIn(int seconds) {
        Logger.debug("Reconnecting");
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Logger.debug("Failed: " + e.getLocalizedMessage());
    }
}
