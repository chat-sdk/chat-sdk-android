package app.xmpp.adapter.listeners;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import java.lang.ref.WeakReference;

import sdk.chat.core.session.ChatSDK;
import app.xmpp.adapter.XMPPManager;
import app.xmpp.adapter.enums.ConnectionStatus;
import sdk.guru.common.RX;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class XMPPConnectionListener implements ConnectionListener {

    public BehaviorSubject<ConnectionStatus> connectionStatusSource = BehaviorSubject.create();
    public XMPPRosterListener rosterListener;
    public WeakReference<XMPPManager> manager;

    public XMPPConnectionListener (XMPPManager manager) {
        rosterListener = new XMPPRosterListener(manager);
        connectionStatusSource.subscribeOn(RX.io());
        this.manager = new WeakReference<>(manager);
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        connection.addConnectionListener(this);
        connectionStatusSource.onNext(ConnectionStatus.Authenticated);
    }

    @Override
    public void connected(XMPPConnection connection) {
        connection.addConnectionListener(this);
        connectionStatusSource.onNext(ConnectionStatus.Connected);

//        Presence presence = PresenceHelper.presenceForUser(ChatSDK.currentUser());
//        manager.get().sendPresence(presence);
//        manager.get().mucManager.joinExistingGroupThreads();


    }

    @Override
    public void connectionClosed() {
        connectionStatusSource.onNext(ConnectionStatus.Disconnected);
    }

//    @Override
//    public void reconnectingIn(int seconds) {
//        connectionStatusSource.onNext(ConnectionStatus.Reconnecting);
//    }
//
//    @Override
//    public void reconnectionSuccessful() {
//        connectionStatusSource.onNext(ConnectionStatus.Connected);
//    }
//
//    @Override
//    public void reconnectionFailed(Exception e) {
//        ChatSDK.logError(e);
//        // TODO: When this is included, it can cause a crash
//        //connectionStatusSource.onError(e);
//        connectionStatusSource.onNext(ConnectionStatus.Disconnected);
//    }

    @Override
    public void connectionClosedOnError(Exception e){
        ChatSDK.events().onError(e);
        connectionStatusSource.onNext(ConnectionStatus.Disconnected);
    }
}
