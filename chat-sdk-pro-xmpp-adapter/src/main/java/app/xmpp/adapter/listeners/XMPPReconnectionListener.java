package app.xmpp.adapter.listeners;

import org.jivesoftware.smack.ReconnectionListener;
import org.pmw.tinylog.Logger;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import sdk.chat.core.session.ChatSDK;


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

