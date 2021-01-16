package app.xmpp.adapter.message.queue;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.xmpp.adapter.defines.XMPPDefines;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.session.ChatSDK;

// Seems not to be needed
@Deprecated
public class OutgoingStanzaQueue implements ConnectionListener, StanzaListener, StanzaFilter {

    protected List<OutgoingStanza> stanzaQueue = new ArrayList<>();

    protected WeakReference<XMPPTCPConnection> connection;
    protected boolean enabled = false;

    protected Disposable timerDisposable;

    public OutgoingStanzaQueue() {
        timerDisposable = Observable.interval(3, TimeUnit.SECONDS).subscribe(aLong -> {
            send();
        });
    }

    public void clear() {
        List<OutgoingStanza> toRemove = new ArrayList<>();
        for (OutgoingStanza stanza: stanzaQueue) {
            if (stanza.isSent()) {
                toRemove.add(stanza);
            }
        }
        for(OutgoingStanza stanza: toRemove) {
            stanzaQueue.remove(stanza);
        }
    }

    public void handleAck(String elementID) {
        for (OutgoingStanza stanza: stanzaQueue) {
            if (stanza.elementID().equals(elementID)) {
                stanza.markSent();
            }
        }
    }

    public void send() {
        for (OutgoingStanza stanza: stanzaQueue) {
            if (!stanza.isSent() && stanza.isDue()) {
                stanza.willTrySend();
                try {
                    connection.get().sendStanza(stanza.stanza);
                } catch (Exception e) {
                    ChatSDK.events().onError(e);
                }
            }
        }
        clear();
    }

    public void add(Stanza stanza) {
        if (isEnabled() && accept(stanza)) {
            for (OutgoingStanza os: stanzaQueue) {
                if (stanza.getStanzaId().equals(os.elementID())) {
                    return;
                }
                stanzaQueue.add(new OutgoingStanza(stanza));
            }
        }
    }

    @Override
    public void connected(XMPPConnection connection) {
        // Stream management only works for TCP connections
        if (connection instanceof XMPPTCPConnection) {
            this.connection = new WeakReference<>((XMPPTCPConnection) connection);
        }
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        if (this.connection.get().isSmEnabled()) {
            connection.removeStanzaSendingListener(this);
            connection.addStanzaSendingListener(this, this);
            enabled = true;
        }
    }

    @Override
    public void connectionClosed() {
    }

    @Override
    public void connectionClosedOnError(Exception e) {
    }

    @Override
    public void processStanza(Stanza packet) {
        try {
            connection.get().addStanzaIdAcknowledgedListener(packet.getStanzaId(), ack -> {
                handleAck(ack.getStanzaId());
            });
        } catch (Exception e) {
            ChatSDK.events().onError(e);
        }
        add(packet);
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Stanza filter for which outgoing stanzas to add
    @Override
    public boolean accept(Stanza stanza) {
        if (stanza instanceof Message) {
            Message message = (Message) stanza;

            boolean hasNoRetryExtension = stanza.getExtension(XMPPDefines.Extras, XMPPDefines.NoRetryNamespace) != null;
            if (hasNoRetryExtension) {
                return false;
            }

            boolean isReadExtension = message.getExtension(XMPPDefines.Extras, XMPPDefines.MessageReadNamespace) != null;
            boolean hasBody = message.getBody() != null;

            return isReadExtension || hasBody;
        }
        return false;
    }
}
