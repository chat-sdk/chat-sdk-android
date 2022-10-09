package app.xmpp.adapter.listeners;

import org.jivesoftware.smackx.ping.PingFailedListener;

import java.lang.ref.WeakReference;

import app.xmpp.adapter.XMPPManager;

@Deprecated
public class XMPPPingListener implements PingFailedListener {

    protected WeakReference<XMPPManager> manager;
    protected int failCount = 0;

    public XMPPPingListener(XMPPManager manager) {
        this.manager = new WeakReference<>(manager);
    }

    @Override
    public void pingFailed() {
        failCount++;

        if (failCount > 1) {
            failCount = 0;
            XMPPManager.shared().hardReconnect();
        }

    }
}
