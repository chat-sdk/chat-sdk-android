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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import app.xmpp.adapter.defines.XMPPDefines;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.session.ChatSDK;

// Seems not to be needed
public class OutgoingStanzaQueue implements ConnectionListener, StanzaListener, StanzaFilter {

    protected List<OutgoingStanza> stanzaQueue = new ArrayList<>();
    protected Map<String, OutgoingStanza> sending = new HashMap<>();

    protected WeakReference<XMPPTCPConnection> connection;
    protected boolean enabled = false;
    protected boolean running = false;

    protected Disposable timerDisposable;

    public OutgoingStanzaQueue() {
//        timerDisposable = Observable.interval(3, TimeUnit.SECONDS).subscribe(aLong -> {
//            send();
//        });
    }

    public void pause() {
        if (timerDisposable != null) {
            timerDisposable.dispose();
            timerDisposable = null;
        }
        running = false;
    }

    public void start() {
        pause();
        timerDisposable = Observable.interval(3, TimeUnit.SECONDS).subscribe(aLong -> {
            send();
        });
        running = true;
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
                sending.remove(elementID);
            }
        }
    }

    public void send() {
        if (getConnection() != null && getConnection().isAuthenticated()) {
            for (OutgoingStanza stanza: stanzaQueue) {
                if (!stanza.isSent() && stanza.isDue()) {
                    stanza.willTrySend();
                    try {

                        try {
                            getConnection().addStanzaIdAcknowledgedListener(stanza.stanza.getStanzaId(), ack -> {
                                handleAck(ack.getStanzaId());
                            });
                        } catch (Exception e) {
                            ChatSDK.events().onError(e);
                        }

                        connection.get().sendStanza(stanza.stanza);
                        sending.put(stanza.elementID(), stanza);
                    } catch (Exception e) {

                        ChatSDK.events().onError(e);
                    }
                }
            }
            clear();
        }
    }

    public boolean add(Stanza stanza) {
        if (isEnabled() && accept(stanza)) {
            for (OutgoingStanza os: stanzaQueue) {
                if (stanza.getStanzaId().equals(os.elementID())) {
                    return false;
                }
            }
            stanzaQueue.add(new OutgoingStanza(stanza));
            return true;
        }
        return false;
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
        if (connection instanceof XMPPTCPConnection && ((XMPPTCPConnection) connection).isSmEnabled()) {
            connection.removeStanzaSendingListener(this);
            connection.addStanzaSendingListener(this, this);
            enabled = true;
            start();
        }
    }

    @Override
    public void connectionClosed() {
        pause();
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        pause();
    }

    @Override
    public void processStanza(Stanza packet) {
        if (add(packet)) {
//            try {
//                if (getConnection() != null) {
//                    getConnection().addStanzaIdAcknowledgedListener(packet.getStanzaId(), ack -> {
//                        handleAck(ack.getStanzaId());
//                    });
//                }
//            } catch (Exception e) {
//                ChatSDK.events().onError(e);
//            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Stanza filter for which outgoing stanzas to add
    @Override
    public boolean accept(Stanza stanza) {
        if (stanza instanceof Message) {
            Message message = (Message) stanza;

            if (stanza.getStanzaId() == null) {
                return false;
            }

            if (sending.containsKey(stanza.getStanzaId())) {
                return false;
            }

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

    public XMPPTCPConnection getConnection() {
        if (connection != null) {
            return connection.get();
        }
        return null;
    }
}
